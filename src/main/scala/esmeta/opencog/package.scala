package esmeta.opencog

import esmeta.cfg.CFG
import esmeta.spec.Spec
import esmeta.ir.Program

/** OpenCog cognitive architecture for ECMAScript specification analysis */

/** AtomSpace representation using ECMA-262 constructs */
case class AtomSpace(
  atoms: Set[Atom] = Set(),
  truthValues: Map[Atom, TruthValue] = Map(),
  attentionValues: Map[Atom, AttentionValue] = Map()
) {
  /** Add an atom to the AtomSpace */
  def addAtom(atom: Atom, tv: Option[TruthValue] = None, av: Option[AttentionValue] = None): AtomSpace =
    copy(
      atoms = atoms + atom,
      truthValues = tv.fold(truthValues)(t => truthValues + (atom -> t)),
      attentionValues = av.fold(attentionValues)(a => attentionValues + (atom -> a))
    )
    
  /** Find atoms matching a pattern */
  def findAtoms(pattern: AtomPattern): Set[Atom] =
    atoms.filter(pattern.matches)
    
  /** Get attention value for an atom */
  def getAttentionValue(atom: Atom): AttentionValue =
    attentionValues.getOrElse(atom, AttentionValue.default)
    
  /** Get truth value for an atom */
  def getTruthValue(atom: Atom): TruthValue =
    truthValues.getOrElse(atom, TruthValue.default)
}

/** Base trait for all atoms in the cognitive architecture */
sealed trait Atom {
  def atomType: AtomType
  def name: String
}

/** Types of atoms in the OpenCog system */
enum AtomType {
  case ConceptNode
  case PredicateNode 
  case ExecutionLink
  case EvaluationLink
  case InheritanceLink
  case SimilarityLink
  case ImplicationLink
  case EquivalenceLink
  case AndLink
  case OrLink
  case NotLink
  case ListLink
  case LambdaLink
  case VariableNode
  case NumberNode
  case SchemaNode
}

/** Node atoms represent concepts or entities */
case class Node(atomType: AtomType, name: String, value: Option[Any] = None) extends Atom

/** Link atoms represent relationships between atoms */
case class Link(atomType: AtomType, name: String, outgoing: List[Atom]) extends Atom

/** Truth value representing confidence and strength */
case class TruthValue(strength: Double, confidence: Double) {
  require(strength >= 0.0 && strength <= 1.0, "Strength must be between 0 and 1")
  require(confidence >= 0.0 && confidence <= 1.0, "Confidence must be between 0 and 1")
}

object TruthValue {
  val default = TruthValue(0.5, 0.5)
  val TRUE = TruthValue(1.0, 1.0)
  val FALSE = TruthValue(0.0, 1.0)
  val UNKNOWN = TruthValue(0.5, 0.0)
}

/** Attention value for managing cognitive focus */
case class AttentionValue(sti: Double, lti: Double, vlti: Double = 0.0) {
  /** Short-term importance */
  def shortTermImportance: Double = sti
  /** Long-term importance */  
  def longTermImportance: Double = lti
  /** Very long-term importance */
  def veryLongTermImportance: Double = vlti
}

object AttentionValue {
  val default = AttentionValue(0.0, 0.0, 0.0)
}

/** Pattern matching for atoms */
trait AtomPattern {
  def matches(atom: Atom): Boolean
}

case class TypePattern(atomType: AtomType) extends AtomPattern {
  def matches(atom: Atom): Boolean = atom.atomType == atomType
}

case class NamePattern(name: String) extends AtomPattern {
  def matches(atom: Atom): Boolean = atom.name == name
}

case class CompositePattern(patterns: List[AtomPattern]) extends AtomPattern {
  def matches(atom: Atom): Boolean = patterns.forall(_.matches(atom))
}