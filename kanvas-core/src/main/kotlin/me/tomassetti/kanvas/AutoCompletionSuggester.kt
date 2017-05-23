package me.tomassetti.kanvas

import me.tomassetti.kolasu.model.Node
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.atn.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.*

interface TokenType {
    val type : Int
}

interface EditorContext {
    fun preceedingTokens() : List<Token>
    fun cachedAst() : Node?
}

data class AutoCompletionContext(val preecedingTokens: List<Token>,
                                 val proposals: Set<Pair<TokenType, ParserStack>>,
                                 val cachedAst: Node?)

/**
 * The goal of this is to find the type of tokens that can be used in a given context
 */
interface AutoCompletionSuggester {
    fun autoCompletionContext(editorContext: EditorContext) : AutoCompletionContext
}

data class TokenTypeImpl(override val type: Int) : TokenType {
}

class EditorContextImpl(val code: String, val antlrLexerFactory: AntlrLexerFactory, val textPanel: TextPanel) : EditorContext {
    override fun cachedAst(): Node? {
        return textPanel.cachedRoot
    }

    override fun preceedingTokens(): List<Token> {
        val lexer = antlrLexerFactory.create(code)
        return lexer.toList()
    }
}

class AutoCompletionContextProvider(val ruleNames: Array<String>,
                                    val vocabulary: Vocabulary, val atn: ATN,
                                    val debugging: Boolean = false) : AutoCompletionSuggester {

    override fun autoCompletionContext(editorContext: EditorContext): AutoCompletionContext {
        val preceedingTokens = editorContext.preceedingTokens()
        val collector = Collector()
        process(ruleNames, vocabulary, atn.states[0],
                MyTokenStream(preceedingTokens), collector, ParserStack(ruleNames, vocabulary),
                debugging = debugging)
        return AutoCompletionContext(preceedingTokens, collector.collected(), editorContext.cachedAst())
    }

}

fun String.toInputStream() = ByteArrayInputStream(this.toByteArray(StandardCharsets.UTF_8))

val CARET_TOKEN_TYPE = -10

// Visible for testing
class MyTokenStream(val tokens: List<Token>, val start: Int = 0) {
    fun next() : Token {
        if (start >= tokens.size) {
            return CommonToken(-1)
        } else {
            return tokens[start]
        }
    }
    fun atCaret() : Boolean = next().type < 0
    fun move() : MyTokenStream = MyTokenStream(tokens, start + 1)
}

// Visible for testing
class Collector {
    private val collected = HashSet<Pair<TokenType, ParserStack>>()
    fun collect(type: Int, parserStack: ParserStack) {
        collected.add(Pair(TokenTypeImpl(type), parserStack))
    }
    fun collected() : Set<Pair<TokenType, ParserStack>> = collected
}

private fun describe(ruleNames: Array<String>, vocabulary: Vocabulary,
                     s: ATNState, t: Transition) =
        "[${s.stateNumber}] ${s.describe(ruleNames)} TR ${t.describe(ruleNames, vocabulary)}"

fun <E> List<out E>.minusLast() : List<E> = this.subList(0, this.size - 1)

class ParserStack(val ruleNames: Array<String>, val vocabulary: Vocabulary,
                  val states : List<ATNState> = emptyList()) {

    fun process(state: ATNState) : Pair<Boolean, ParserStack> {
        return when (state) {
            is RuleStartState, is StarBlockStartState, is BasicBlockStartState,
            is PlusBlockStartState, is StarLoopEntryState -> {
                return Pair(true, ParserStack(ruleNames, vocabulary, states.plus(state)))
            }
            is BlockEndState -> {
                if (states.last() == state.startState) {
                    return Pair(true, ParserStack(ruleNames, vocabulary, states.minusLast()))
                } else {
                    return Pair(false, this)
                }
            }
            is LoopEndState -> {
                val cont = states.last() is StarLoopEntryState
                        && (states.last() as StarLoopEntryState).loopBackState == state.loopBackState
                if (cont) {
                    return Pair(true, ParserStack(ruleNames, vocabulary, states.minusLast()))
                } else {
                    return Pair(false, this)
                }
            }
            is RuleStopState -> {
                val cont = states.last() is RuleStartState
                        && (states.last() as RuleStartState).stopState == state
                if (cont) {
                    return Pair(true, ParserStack(ruleNames, vocabulary, states.minusLast()))
                } else {
                    return Pair(false, this)
                }
            }
            is StarLoopbackState -> {
                val cont = states.last() is StarLoopEntryState
                        && (states.last() as StarLoopEntryState).loopBackState == state
                if (cont) {
                    return Pair(true, ParserStack(ruleNames, vocabulary, states.minusLast()))
                } else {
                    return Pair(false, this)
                }
            }
            is BasicState,is StarLoopbackState, is PlusLoopbackState/*, is StarLoopEntryState*/ -> return Pair(true, this)
            else -> throw UnsupportedOperationException("Unsupported state: ${state.javaClass.canonicalName}")
        }
    }

    fun describe() = "[${states.map { it.describe(ruleNames) }.joinToString(separator = ", ")}]"

    /**
     * The rules in which I am at the moment
     */
    fun rulesStack() : List<String> {
        val rules = LinkedList<String>()
        states.filter { it.isRuleStart() }.forEach { rules.add(ruleNames[it.ruleIndex]) }
        return rules
    }

    fun rulesWeAreIn() = states.filter { it.isRuleStart() }.map { it.ruleIndex }
}

private fun isCompatibleWithStack(state: ATNState, parserStack:ParserStack) : Boolean {
    //println("isCompatibleWithStack state=${state.me.tomassetti.kanvas.describe()} parserStack=${parserStack.me.tomassetti.kanvas.describe()}")
    val res = parserStack.process(state)
    if (!res.first) {
        return false
    }
    if (state.epsilonOnlyTransitions) {
        return state.transitions.any { isCompatibleWithStack(it.target, res.second) }
    } else {
        return true
    }
}

private fun canGoThere(parserStack: ParserStack, currentState: ATNState, targetState: ATNState) : Boolean {
    val currentRule = if (currentState is RuleStopState) {
        val rulesWeAreIn = parserStack.rulesWeAreIn()
        if (rulesWeAreIn.size < 2) throw IllegalStateException()
        rulesWeAreIn.get(rulesWeAreIn.size - 2)
    } else {
        currentState.ruleIndex
    }
    if (targetState is RuleStartState) {
        return true
    } else {
        return targetState.ruleIndex == currentRule
    }
}

// Visible for testing
fun process(ruleNames: Array<String>, vocabulary: Vocabulary,
                    state: ATNState, tokens: MyTokenStream, collector: Collector,
                    parserStack: ParserStack,
                    alreadyPassed: Set<Int> = HashSet<Int>(),
                    history : List<String> = listOf("start"),
                    debugging : Boolean = false) {
    if (debugging) {
        println("PROCESSING state=${state.describe(ruleNames)}")
        println("\tparserStack=${parserStack.describe()}")
        println("\talreadyPassed=$alreadyPassed")
        println("\thistory=${history.joinToString(", ")}")
    }

    val atCaret = tokens.atCaret()
    if (debugging) {
        println("\tatCaret=$atCaret")
        if (!atCaret) {
            println("\tnext token = ${tokens.next()}")
        }
    }
    val stackRes = parserStack.process(state)
    if (!stackRes.first) {
        if (debugging) {
            println("\tinvalid stack, returning")
        }
        return
    }

    if (debugging) {
        state.transitions.forEach {
            println("\t\ttransition: ${it.describe(ruleNames, vocabulary)}")
        }
    }

    state.transitions.forEach {
        val desc = describe(ruleNames, vocabulary, state, it)
        when {
            it.isEpsilon -> {
                if (!alreadyPassed.contains(it.target.stateNumber)) {
                    if (canGoThere(parserStack, state, it.target)) {
                        process(ruleNames, vocabulary, it.target, tokens, collector, stackRes.second,
                                alreadyPassed.plus(it.target.stateNumber),
                                history.plus(desc), debugging = debugging)
                    }
                }
            }
            it is AtomTransition -> {
                val nextTokenType = tokens.next()
                if (canGoThere(parserStack, state, it.target)) {
                    if (atCaret) {
                        if (isCompatibleWithStack(it.target, parserStack)) {
                            collector.collect(it.label, parserStack)
                        } else if (debugging) {
                            println("\tNOT COMPATIBLE")
                        }
                    } else {
                        if (nextTokenType.type == it.label) {
                            process(ruleNames, vocabulary, it.target, tokens.move(), collector, stackRes.second, HashSet<Int>(), history.plus(desc), debugging = debugging)
                        }
                    }
                }
            }
            it is SetTransition -> {
                val nextTokenType = tokens.next()
                it.label().toList().forEach { sym ->
                    if (canGoThere(parserStack, state, it.target)) {
                        if (atCaret) {
                            if (isCompatibleWithStack(it.target, parserStack)) {
                                collector.collect(sym, parserStack)
                            } else if (debugging) {
                                println("\tNOT COMPATIBLE")
                            }
                        } else {
                            if (nextTokenType.type == sym) {
                                process(ruleNames, vocabulary, it.target, tokens.move(), collector, stackRes.second, HashSet<Int>(), history.plus(desc), debugging = debugging)
                            }
                        }
                    }
                }
            }
            else -> throw UnsupportedOperationException("Transition not supported ${it.javaClass.canonicalName}")
        }
    }
}

fun Lexer.toList() : List<Token> {
    val res = LinkedList<Token>()
    do {
        var next = this.nextToken()
        if (next.channel == 0) {
            if (next.type < 0) {
                next = CommonToken(CARET_TOKEN_TYPE)
            }
            res.add(next)
        }
    } while (next.type >= 0)
    return res
}
