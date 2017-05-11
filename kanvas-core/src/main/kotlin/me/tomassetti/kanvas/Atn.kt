package me.tomassetti.kanvas

import javassist.ClassPool
import javassist.NotFoundException
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.atn.*
import java.util.*

private fun describeRule(ruleIndex: Int, ruleNames: Array<String>?)
    = if (ruleNames == null) "ruleIndex=$ruleIndex" else "ruleName=${ruleNames[ruleIndex]}"

fun ATNState.describe(ruleNames: Array<String>? = null) : String = "[${this.stateNumber}] " + when(this) {
    is RuleStartState -> "rule start (stop -> ${this.stopState}) ${if (this.isLeftRecursiveRule) "leftRec " else ""}(${describeRule(this.ruleIndex, ruleNames)})"
    is RuleStopState -> "rule stop (${describeRule(this.ruleIndex, ruleNames)})"
    is BasicState -> "basic"
    is PlusBlockStartState -> "plus block start (loopback ${this.loopBackState})"
    is StarBlockStartState -> "star block start"
    is StarLoopEntryState -> "star loop entry start (loopback ${this.loopBackState}) prec ${this.isPrecedenceDecision}"
    is StarLoopbackState -> "star loopback"
    is BasicBlockStartState -> "basic block start"
    is BlockEndState -> "block end (start ${this.startState})"
    is PlusLoopbackState -> "plus loopback"
    is LoopEndState -> "loop end (loopback ${this.loopBackState})"
    is TokensStartState -> "tokens start state ${this.stateNumber} ${describeRule(this.ruleIndex, ruleNames)}"
    else -> "UNKNOWN ${this.javaClass.simpleName}"
}

fun Transition.describe(ruleNames: Array<String>, vocabulary: Vocabulary) : String = when(this) {
    is EpsilonTransition -> "(e)"
    is RuleTransition -> "rule ${ruleNames[this.ruleIndex]} precedence ${this.precedence}"
    is AtomTransition -> "atom(${vocabulary.getSymbolicName(this.label)})"
    is SetTransition -> "set(${this.set.toList().map { vocabulary.getSymbolicName(it) }.joinToString(", ")})"
    is ActionTransition -> "action"
    is PrecedencePredicateTransition -> "precedence predicate ${this.precedence}"
    else -> "UNKNOWN ${this.javaClass.simpleName}"
} + " to [${this.target}]"

fun printAtn(atn: ATN, ruleNames: Array<String>, vocabulary: Vocabulary) {
    atn.states.forEach { s ->
        println("[${s.stateNumber} ${ruleNames[s.ruleIndex]}] ${s.describe(ruleNames)}")
        s.transitions.forEach { t ->
            println("  ${t.describe(ruleNames, vocabulary)} -> [${t.target.stateNumber}] ${t.target.describe(ruleNames)}")
        }
    }
}

private fun String.toRuleName(parserClass: Class<*>) = this.removePrefix(parserClass.simpleName + "$").removeSuffix("Context")

/**
 * Find the subrules: a map that list by the superrule the list of descending subrules, in order
 */
fun subRules(parserClass: Class<*>): Map<String, MutableList<String>> {
    val pool = ClassPool.getDefault()
    val rulesByLine = HashMap<String, Int>()
    val subRules = HashMap<String, MutableList<String>>()
    parserClass.classes.forEach { nestedClass ->
        if (!nestedClass.superclass.name.equals(ParserRuleContext::class.java.canonicalName)
                && !nestedClass.superclass.name.equals(Any::class.java.canonicalName)) {
            val superRule = nestedClass.superclass.simpleName.toRuleName(parserClass)
            val subRule = nestedClass.simpleName.toRuleName(parserClass)
            if (!subRules.containsKey(superRule)) {
                subRules[superRule] = LinkedList<String>()
            }
            subRules[superRule]!!.add(subRule)
        }
        try {
            val cc = pool.get(parserClass.canonicalName + "$${nestedClass.simpleName}")
            val name = cc.simpleName.toRuleName(parserClass)
            rulesByLine[name] = cc.constructors.first().methodInfo2.getLineNumber(0)
        } catch (e: NotFoundException) {
            // skip
        }
    }
    subRules.forEach { superRule, subRules ->
        Collections.sort(subRules, { a, b ->
            rulesByLine[a]!! - rulesByLine[b]!! })
    }
    return subRules
}
