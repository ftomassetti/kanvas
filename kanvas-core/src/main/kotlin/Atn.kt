import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.atn.*

fun ATNState.describe() : String = when(this) {
    is RuleStartState -> "rule start (stop -> ${this.stopState}) ${if (this.isLeftRecursiveRule) "leftRec " else ""}(ruleIndex=${this.ruleIndex})"
    is RuleStopState -> "rule stop (ruleIndex=${this.ruleIndex})"
    is BasicState -> "basic"
    is PlusBlockStartState -> "plus block start (loopback ${this.loopBackState})"
    is StarBlockStartState -> "star block start"
    is StarLoopEntryState -> "star loop entry start (loopback ${this.loopBackState}) prec ${this.isPrecedenceDecision}"
    is StarLoopbackState -> "star loopback"
    is BasicBlockStartState -> "basic block start"
    is BlockEndState -> "block end (start ${this.startState})"
    is PlusLoopbackState -> "plus loopback"
    is LoopEndState -> "loop end (loopback ${this.loopBackState})"
    is TokensStartState -> "tokens start state ${this.stateNumber} ruleIndex=${this.ruleIndex}"
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
}

fun printAtn(atn: ATN, ruleNames: Array<String>, vocabulary: Vocabulary) {
    atn.states.forEach { s ->
        println("[${s.stateNumber} ${ruleNames[s.ruleIndex]}] ${s.describe()}")
        s.transitions.forEach { t ->
            println("  ${t.describe(ruleNames, vocabulary)} -> [${t.target.stateNumber}] ${t.target.describe()}")
        }
    }
}