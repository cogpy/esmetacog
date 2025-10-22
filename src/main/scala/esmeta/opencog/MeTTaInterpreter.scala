package esmeta.opencog

import scala.util.{Try, Success, Failure}
import scala.util.parsing.combinator.*

/** MeTTa (Meta Type Talk) interpreter for cognitive scripting */
class MeTTaInterpreter extends RegexParsers {
  
  /** Execute a MeTTa script in the context of an AtomSpace */
  def execute(script: String, atomSpace: AtomSpace): MeTTaResult = {
    val executionTrace = scala.collection.mutable.ListBuffer[String]()
    val newAtoms = scala.collection.mutable.Set[Atom]()
    
    try {
      val expressions = parseScript(script)
      var result: Any = ()
      
      for (expr <- expressions) {
        executionTrace += s"Executing: ${expr}"
        result = evaluateExpression(expr, atomSpace, newAtoms)
        executionTrace += s"Result: ${result}"
      }
      
      MeTTaResult(result, newAtoms.toSet, executionTrace.toList)
    } catch {
      case e: Exception =>
        executionTrace += s"Error: ${e.getMessage}"
        MeTTaResult(e.getMessage, newAtoms.toSet, executionTrace.toList)
    }
  }
  
  /** Parse MeTTa script into expressions */
  private def parseScript(script: String): List[MeTTaExpression] = {
    parseAll(program, script) match {
      case Success(exprs, _) => exprs
      case Failure(msg, _) => throw new IllegalArgumentException(s"Parse error: $msg")
      case Error(msg, _) => throw new IllegalArgumentException(s"Parse error: $msg")
    }
  }
  
  /** Grammar for MeTTa expressions */
  private def program: Parser[List[MeTTaExpression]] = rep(expression)
  
  private def expression: Parser[MeTTaExpression] = 
    atomExpression | queryExpression | assertExpression | lambdaExpression
  
  private def atomExpression: Parser[AtomExpression] = 
    "(" ~> atomType ~ identifier ~ opt(valueList) <~ ")" ^^ {
      case atype ~ name ~ values => AtomExpression(atype, name, values.getOrElse(Nil))
    }
  
  private def queryExpression: Parser[QueryExpression] =
    "(?=" ~> atomPattern <~ ")" ^^ QueryExpression
  
  private def assertExpression: Parser[AssertExpression] =
    "(assert" ~> expression <~ ")" ^^ AssertExpression
  
  private def lambdaExpression: Parser[LambdaExpression] =
    "(lambda" ~> "(" ~> rep(identifier) <~ ")" ~ expression <~ ")" ^^ (_ => 
      LambdaExpression(List("x"), AtomExpression(AtomType.ConceptNode, "lambda", Nil))
    )
  
  private def atomPattern: Parser[AtomPattern] =
    atomType ^^ TypePattern |
    identifier ^^ NamePattern
  
  private def atomType: Parser[AtomType] = 
    "ConceptNode" ^^^ AtomType.ConceptNode |
    "PredicateNode" ^^^ AtomType.PredicateNode |
    "ExecutionLink" ^^^ AtomType.ExecutionLink |
    "EvaluationLink" ^^^ AtomType.EvaluationLink |
    "InheritanceLink" ^^^ AtomType.InheritanceLink |
    "SimilarityLink" ^^^ AtomType.SimilarityLink |
    "ImplicationLink" ^^^ AtomType.ImplicationLink |
    "LambdaLink" ^^^ AtomType.LambdaLink |
    "VariableNode" ^^^ AtomType.VariableNode |
    "NumberNode" ^^^ AtomType.NumberNode |
    "SchemaNode" ^^^ AtomType.SchemaNode
  
  private def valueList: Parser[List[Any]] = 
    "[" ~> repsep(value, ",") <~ "]"
  
  private def value: Parser[Any] = 
    """[-]?\d+\.\d+""".r ^^ (_.toDouble) |
    """[-]?\d+""".r ^^ (_.toInt) |
    "\"" ~> """[^"]*""".r <~ "\"" |
    identifier
  
  private def identifier: Parser[String] = """[a-zA-Z_][a-zA-Z0-9_]*""".r
  
  /** Evaluate a MeTTa expression */
  private def evaluateExpression(
    expr: MeTTaExpression, 
    atomSpace: AtomSpace,
    newAtoms: scala.collection.mutable.Set[Atom]
  ): Any = {
    expr match {
      case AtomExpression(atomType, name, values) =>
        val atom = createAtom(atomType, name, values)
        newAtoms += atom
        atom
        
      case QueryExpression(pattern) =>
        atomSpace.findAtoms(pattern)
        
      case AssertExpression(innerExpr) =>
        evaluateExpression(innerExpr, atomSpace, newAtoms)
        
      case LambdaExpression(variables, body) =>
        // Create a lambda function representation
        Link(AtomType.LambdaLink, s"lambda_${variables.mkString("_")}", 
             variables.map(v => Node(AtomType.VariableNode, v)))
    }
  }
  
  /** Create an atom from MeTTa expression components */
  private def createAtom(atomType: AtomType, name: String, values: List[Any]): Atom = {
    atomType match {
      case AtomType.ConceptNode | AtomType.PredicateNode | AtomType.VariableNode | 
           AtomType.NumberNode | AtomType.SchemaNode =>
        Node(atomType, name, values.headOption)
        
      case _ =>
        // Link types need outgoing atoms
        val outgoingAtoms = values.collect {
          case atomName: String => Node(AtomType.ConceptNode, atomName)
        }
        Link(atomType, name, outgoingAtoms)
    }
  }
}

/** Base trait for MeTTa expressions */
sealed trait MeTTaExpression

/** Atom creation expression */
case class AtomExpression(atomType: AtomType, name: String, values: List[Any]) extends MeTTaExpression

/** Query expression for finding atoms */
case class QueryExpression(pattern: AtomPattern) extends MeTTaExpression

/** Assert expression for adding knowledge */
case class AssertExpression(expression: MeTTaExpression) extends MeTTaExpression

/** Lambda expression for defining functions */
case class LambdaExpression(variables: List[String], body: MeTTaExpression) extends MeTTaExpression