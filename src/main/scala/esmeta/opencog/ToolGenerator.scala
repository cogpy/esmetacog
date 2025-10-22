package esmeta.opencog

import java.io.{File, PrintWriter}
import scala.util.{Try, Success, Failure}

/** Tool generator for creating ECMA-262 based tools from cognitive patterns */
class ToolGenerator(config: ToolGenConfig) {
  
  /** Generate tools from cognitive patterns and tool plan */
  def generateTools(plan: ToolGenerationPlan, patterns: CognitivePatterns): List[GeneratedTool] = {
    val tools = scala.collection.mutable.ListBuffer[GeneratedTool]()
    
    // Generate abstraction tools
    plan.abstractionTools.foreach { abstractionTool =>
      val pattern = patterns.abstractionPatterns.find(_.name.contains(abstractionTool.name))
      pattern.foreach { p =>
        tools += generateAbstractionTool(abstractionTool, p)
      }
    }
    
    // Generate reasoning tools
    plan.reasoningTools.foreach { reasoningTool =>
      val pattern = patterns.reasoningPatterns.find(_.name.contains(reasoningTool.name))
      pattern.foreach { p =>
        tools += generateReasoningTool(reasoningTool, p)
      }
    }
    
    // Generate learning tools
    plan.learningTools.foreach { learningTool =>
      val pattern = patterns.learningPatterns.find(_.name.contains(learningTool.name))
      pattern.foreach { p =>
        tools += generateLearningTool(learningTool, p)
      }
    }
    
    tools.toList
  }
  
  /** Generate an abstraction tool */
  private def generateAbstractionTool(tool: AbstractionTool, pattern: AbstractionPattern): GeneratedTool = {
    val code = generateAbstractionCode(pattern)
    GeneratedTool(
      name = tool.name,
      description = tool.description,
      code = code,
      toolType = ToolType.Abstraction,
      pattern = Some(pattern)
    )
  }
  
  /** Generate a reasoning tool */
  private def generateReasoningTool(tool: ReasoningTool, pattern: ReasoningPattern): GeneratedTool = {
    val code = generateReasoningCode(pattern)
    GeneratedTool(
      name = tool.name,
      description = tool.description,
      code = code,
      toolType = ToolType.Reasoning,
      pattern = Some(pattern)
    )
  }
  
  /** Generate a learning tool */
  private def generateLearningTool(tool: LearningTool, pattern: LearningPattern): GeneratedTool = {
    val code = generateLearningCode(pattern)
    GeneratedTool(
      name = tool.name,
      description = tool.description,
      code = code,
      toolType = ToolType.Learning,
      pattern = Some(pattern)
    )
  }
  
  /** Generate JavaScript code for abstraction patterns */
  private def generateAbstractionCode(pattern: AbstractionPattern): String = {
    val className = sanitizeName(pattern.name)
    val atoms = pattern.atoms.take(5) // Limit for practicality
    
    s"""/**
       | * ${pattern.description}
       | * Generated abstraction tool from OpenCog cognitive analysis
       | */
       |class ${className} {
       |  constructor() {
       |    this.atoms = ${generateAtomArray(atoms)};
       |    this.patterns = new Map();
       |  }
       |  
       |  /**
       |   * Apply abstraction pattern to ECMAScript construct
       |   * @param {Object} construct - ECMAScript language construct
       |   * @returns {Object} Abstracted representation
       |   */
       |  abstract(construct) {
       |    const pattern = this.recognizePattern(construct);
       |    if (pattern) {
       |      return this.applyAbstraction(pattern, construct);
       |    }
       |    return construct;
       |  }
       |  
       |  /**
       |   * Recognize cognitive pattern in construct
       |   */
       |  recognizePattern(construct) {
       |    // Pattern recognition logic based on atom structure
       |    for (const atom of this.atoms) {
       |      if (this.matchesAtom(construct, atom)) {
       |        return atom;
       |      }
       |    }
       |    return null;
       |  }
       |  
       |  /**
       |   * Apply abstraction transformation
       |   */
       |  applyAbstraction(pattern, construct) {
       |    return {
       |      type: 'Abstraction',
       |      pattern: pattern.name,
       |      original: construct,
       |      cognitive_value: this.computeCognitiveValue(construct)
       |    };
       |  }
       |  
       |  /**
       |   * Check if construct matches atom pattern
       |   */
       |  matchesAtom(construct, atom) {
       |    // Simple structural matching
       |    return construct.type && atom.name.includes(construct.type);
       |  }
       |  
       |  /**
       |   * Compute cognitive value for construct
       |   */
       |  computeCognitiveValue(construct) {
       |    // Cognitive assessment based on complexity and patterns
       |    const complexity = this.measureComplexity(construct);
       |    const abstraction_level = this.measureAbstraction(construct);
       |    return { complexity, abstraction_level };
       |  }
       |  
       |  measureComplexity(construct) {
       |    return JSON.stringify(construct).length / 100;
       |  }
       |  
       |  measureAbstraction(construct) {
       |    return construct.type ? 1.0 : 0.5;
       |  }
       |}
       |
       |module.exports = ${className};
       |""".stripMargin
  }
  
  /** Generate JavaScript code for reasoning patterns */
  private def generateReasoningCode(pattern: ReasoningPattern): String = {
    val className = sanitizeName(pattern.name)
    val rules = pattern.rules.take(3) // Limit for practicality
    
    s"""/**
       | * ${pattern.description}
       | * Generated reasoning tool from OpenCog cognitive analysis
       | */
       |class ${className} {
       |  constructor() {
       |    this.rules = ${generateRuleArray(rules)};
       |    this.inferences = [];
       |  }
       |  
       |  /**
       |   * Apply reasoning rules to ECMAScript specification knowledge
       |   * @param {Object} knowledge - Specification knowledge base
       |   * @returns {Array} Inferred conclusions
       |   */
       |  reason(knowledge) {
       |    const conclusions = [];
       |    
       |    for (const rule of this.rules) {
       |      const inference = this.applyRule(rule, knowledge);
       |      if (inference) {
       |        conclusions.push(inference);
       |        this.inferences.push(inference);
       |      }
       |    }
       |    
       |    return conclusions;
       |  }
       |  
       |  /**
       |   * Apply a single inference rule
       |   */
       |  applyRule(rule, knowledge) {
       |    if (this.checkPremises(rule.premises, knowledge)) {
       |      return {
       |        conclusion: rule.conclusion,
       |        confidence: rule.confidence,
       |        premises: rule.premises,
       |        timestamp: new Date().toISOString()
       |      };
       |    }
       |    return null;
       |  }
       |  
       |  /**
       |   * Check if premises are satisfied in knowledge base
       |   */
       |  checkPremises(premises, knowledge) {
       |    return premises.every(premise => 
       |      this.satisfiesPremise(premise, knowledge)
       |    );
       |  }
       |  
       |  /**
       |   * Check if a single premise is satisfied
       |   */
       |  satisfiesPremise(premise, knowledge) {
       |    // Simple pattern matching against knowledge
       |    return knowledge.some(fact => 
       |      this.matchesFact(premise, fact)
       |    );
       |  }
       |  
       |  /**
       |   * Check if premise matches fact
       |   */
       |  matchesFact(premise, fact) {
       |    return premise.name === fact.name || 
       |           (fact.type && premise.name.includes(fact.type));
       |  }
       |  
       |  /**
       |   * Get reasoning history
       |   */
       |  getInferences() {
       |    return this.inferences;
       |  }
       |  
       |  /**
       |   * Clear reasoning history
       |   */
       |  clearInferences() {
       |    this.inferences = [];
       |  }
       |}
       |
       |module.exports = ${className};
       |""".stripMargin
  }
  
  /** Generate JavaScript code for learning patterns */
  private def generateLearningCode(pattern: LearningPattern): String = {
    val className = sanitizeName(pattern.name)
    
    s"""/**
       | * ${pattern.description}
       | * Generated learning tool from OpenCog cognitive analysis
       | * Learning type: ${pattern.learningType}
       | */
       |class ${className} {
       |  constructor() {
       |    this.learningType = '${pattern.learningType}';
       |    this.knowledgeBase = new Map();
       |    this.learningHistory = [];
       |  }
       |  
       |  /**
       |   * Learn patterns from ECMAScript specification data
       |   * @param {Array} examples - Learning examples
       |   * @returns {Object} Learning result
       |   */
       |  learn(examples) {
       |    const result = {
       |      patterns_learned: 0,
       |      confidence: 0,
       |      new_knowledge: []
       |    };
       |    
       |    switch(this.learningType) {
       |      case 'PatternRecognition':
       |        return this.learnPatterns(examples);
       |      case 'ConceptFormation':
       |        return this.formConcepts(examples);
       |      case 'RuleInduction':
       |        return this.induceRules(examples);
       |      case 'AnalogicalReasoning':
       |        return this.analogicalReasoning(examples);
       |      default:
       |        return result;
       |    }
       |  }
       |  
       |  /**
       |   * Pattern recognition learning
       |   */
       |  learnPatterns(examples) {
       |    const patterns = this.extractPatterns(examples);
       |    patterns.forEach(pattern => {
       |      this.knowledgeBase.set(pattern.signature, pattern);
       |    });
       |    
       |    return {
       |      patterns_learned: patterns.length,
       |      confidence: this.computePatternConfidence(patterns),
       |      new_knowledge: patterns
       |    };
       |  }
       |  
       |  /**
       |   * Concept formation learning
       |   */
       |  formConcepts(examples) {
       |    const concepts = this.clusterExamples(examples);
       |    concepts.forEach(concept => {
       |      this.knowledgeBase.set(concept.name, concept);
       |    });
       |    
       |    return {
       |      patterns_learned: concepts.length,
       |      confidence: 0.8,
       |      new_knowledge: concepts
       |    };
       |  }
       |  
       |  /**
       |   * Rule induction learning
       |   */
       |  induceRules(examples) {
       |    const rules = this.extractRules(examples);
       |    rules.forEach(rule => {
       |      this.knowledgeBase.set(rule.id, rule);
       |    });
       |    
       |    return {
       |      patterns_learned: rules.length,
       |      confidence: this.computeRuleConfidence(rules),
       |      new_knowledge: rules
       |    };
       |  }
       |  
       |  /**
       |   * Analogical reasoning learning
       |   */
       |  analogicalReasoning(examples) {
       |    const analogies = this.findAnalogies(examples);
       |    
       |    return {
       |      patterns_learned: analogies.length,
       |      confidence: 0.7,
       |      new_knowledge: analogies
       |    };
       |  }
       |  
       |  // Helper methods for different learning types
       |  extractPatterns(examples) {
       |    return examples.map((ex, i) => ({
       |      signature: `pattern_$${i}`,
       |      structure: this.analyzeStructure(ex),
       |      frequency: 1
       |    }));
       |  }
       |  
       |  clusterExamples(examples) {
       |    return [{
       |      name: 'GeneralConcept',
       |      instances: examples,
       |      properties: this.extractCommonProperties(examples)
       |    }];
       |  }
       |  
       |  extractRules(examples) {
       |    return examples.map((ex, i) => ({
       |      id: `rule_$${i}`,
       |      condition: ex.condition || 'always',
       |      action: ex.action || 'default',
       |      confidence: 0.5 + Math.random() * 0.5
       |    }));
       |  }
       |  
       |  findAnalogies(examples) {
       |    const analogies = [];
       |    for(let i = 0; i < examples.length - 1; i++) {
       |      for(let j = i + 1; j < examples.length; j++) {
       |        const similarity = this.computeSimilarity(examples[i], examples[j]);
       |        if (similarity > 0.5) {
       |          analogies.push({
       |            source: examples[i],
       |            target: examples[j],
       |            similarity: similarity
       |          });
       |        }
       |      }
       |    }
       |    return analogies;
       |  }
       |  
       |  analyzeStructure(example) {
       |    return {
       |      type: example.type || 'unknown',
       |      complexity: JSON.stringify(example).length,
       |      properties: Object.keys(example || {})
       |    };
       |  }
       |  
       |  extractCommonProperties(examples) {
       |    const allProps = examples.flatMap(ex => Object.keys(ex || {}));
       |    const propCounts = allProps.reduce((acc, prop) => {
       |      acc[prop] = (acc[prop] || 0) + 1;
       |      return acc;
       |    }, {});
       |    
       |    return Object.entries(propCounts)
       |      .filter(([prop, count]) => count > examples.length / 2)
       |      .map(([prop, count]) => prop);
       |  }
       |  
       |  computePatternConfidence(patterns) {
       |    return patterns.length > 0 ? 0.6 + (patterns.length * 0.1) : 0;
       |  }
       |  
       |  computeRuleConfidence(rules) {
       |    return rules.reduce((sum, rule) => sum + rule.confidence, 0) / rules.length;
       |  }
       |  
       |  computeSimilarity(a, b) {
       |    const aKeys = Object.keys(a || {});
       |    const bKeys = Object.keys(b || {});
       |    const intersection = aKeys.filter(key => bKeys.includes(key));
       |    const union = [...new Set([...aKeys, ...bKeys])];
       |    return union.length > 0 ? intersection.length / union.length : 0;
       |  }
       |}
       |
       |module.exports = ${className};
       |""".stripMargin
  }
  
  /** Generate atom array representation for JavaScript */
  private def generateAtomArray(atoms: Set[Atom]): String = {
    val atomsJs = atoms.map { atom =>
      s"""{ name: "${atom.name}", type: "${atom.atomType}" }"""
    }.mkString("[", ", ", "]")
    atomsJs
  }
  
  /** Generate rule array representation for JavaScript */
  private def generateRuleArray(rules: List[InferenceRule]): String = {
    val rulesJs = rules.map { rule =>
      val premisesJs = rule.premises.map(p => s"""{ name: "${p.name}" }""").mkString("[", ", ", "]")
      s"""{
         |    premises: ${premisesJs},
         |    conclusion: { name: "${rule.conclusion.name}" },
         |    confidence: ${rule.confidence}
         |  }""".stripMargin
    }.mkString("[", ", ", "]")
    rulesJs
  }
  
  /** Sanitize name for use as JavaScript class name */
  private def sanitizeName(name: String): String = {
    name.replaceAll("[^a-zA-Z0-9_]", "_")
      .replaceAll("^[0-9]", "_")
      .split("_")
      .map(_.capitalize)
      .mkString("")
  }
}

/** Tool types */
enum ToolType {
  case Abstraction
  case Reasoning  
  case Learning
}

/** Generated tool */
case class GeneratedTool(
  name: String,
  description: String,
  code: String,
  toolType: ToolType,
  pattern: Option[Any] = None
) {
  /** Write tool to file */
  def writeToFile(filename: String): Try[Unit] = Try {
    val file = new File(filename)
    file.getParentFile.mkdirs()
    val writer = new PrintWriter(file)
    try {
      writer.println(code)
    } finally {
      writer.close()
    }
  }
}