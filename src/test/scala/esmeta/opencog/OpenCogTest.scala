package esmeta.opencog

import org.scalatest.*
import org.scalatest.funsuite.AnyFunSuite
import esmeta.opencog.*

/** OpenCog basic functionality tests */
class OpenCogTest extends AnyFunSuite {
  
  test("AtomSpace basic operations") {
    val atomSpace = AtomSpace()
    val atom = Node(AtomType.ConceptNode, "TestConcept")
    val tv = TruthValue(0.8, 0.9)
    
    val newSpace = atomSpace.addAtom(atom, Some(tv))
    
    assert(newSpace.atoms.contains(atom))
    assert(newSpace.getTruthValue(atom) == tv)
  }
  
  test("Atom pattern matching") {
    val atomSpace = AtomSpace()
    val concept1 = Node(AtomType.ConceptNode, "Concept1")
    val concept2 = Node(AtomType.ConceptNode, "Concept2")
    val predicate = Node(AtomType.PredicateNode, "Predicate1")
    
    val space = atomSpace
      .addAtom(concept1)
      .addAtom(concept2)
      .addAtom(predicate)
    
    val conceptPattern = TypePattern(AtomType.ConceptNode)
    val matchedAtoms = space.findAtoms(conceptPattern)
    
    assert(matchedAtoms.size == 2)
    assert(matchedAtoms.contains(concept1))
    assert(matchedAtoms.contains(concept2))
  }
  
  test("MeTTa interpreter basic parsing") {
    val interpreter = new MeTTaInterpreter()
    val atomSpace = AtomSpace()
    
    val script = "(ConceptNode \"TestNode\")"
    val result = interpreter.execute(script, atomSpace)
    
    assert(result.newAtoms.nonEmpty)
    assert(result.executionTrace.nonEmpty)
  }
  
  test("Cognitive engine pattern recognition") {
    val engine = new CognitiveEngine()
    val atomSpace = AtomSpace()
      .addAtom(Node(AtomType.ConceptNode, "JavaScript"))
      .addAtom(Node(AtomType.ConceptNode, "ECMAScript"))
      .addAtom(Link(AtomType.SimilarityLink, "JS_ES_Similar", 
                   List(Node(AtomType.ConceptNode, "JavaScript"),
                        Node(AtomType.ConceptNode, "ECMAScript"))))
    
    val patterns = engine.analyzePatterns(atomSpace)
    
    assert(patterns.abstractionPatterns.nonEmpty)
  }
  
  test("Tool generator basic functionality") {
    val config = ToolGenConfig()
    val generator = new ToolGenerator(config)
    
    val pattern = AbstractionPattern("TestAbstraction", "Test pattern", Set(
      Node(AtomType.ConceptNode, "Test")
    ))
    
    val plan = ToolGenerationPlan(
      List(AbstractionTool("TestTool", "Test abstraction tool")),
      Nil,
      Nil
    )
    
    val patterns = CognitivePatterns(List(pattern), Nil, Nil)
    val tools = generator.generateTools(plan, patterns)
    
    assert(tools.nonEmpty)
    assert(tools.head.name == "TestTool")
    assert(tools.head.code.contains("class"))
  }
}