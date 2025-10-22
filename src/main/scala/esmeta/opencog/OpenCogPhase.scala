package esmeta.opencog

import esmeta.cfg.CFG
import esmeta.phase.*
import esmeta.util.{BoolOption, StrOption}
import esmeta.{LOG_DIR, CommandConfig}

/** OpenCog analysis phase */
case object OpenCogAnalyze extends Phase[CFG, OpenCogResult] {
  val name = "opencog-analyze"
  val help = "performs OpenCog cognitive analysis on ECMA-262."
  
  /** phase configuration type */
  type Config = OpenCogConfig

  def apply(
    cfg: CFG,
    cmdConfig: CommandConfig,
    config: Config = defaultConfig,
  ): OpenCogResult = {
    val openCog = new OpenCog()
    val result = openCog.processCFG(cfg)
    
    // Log results if requested
    if (config.log) {
      println("=== OpenCog Cognitive Analysis ===")
      println(s"AtomSpace size: ${result.atomSpace.atoms.size}")
      println(s"Abstraction patterns: ${result.patterns.abstractionPatterns.size}")
      println(s"Reasoning patterns: ${result.patterns.reasoningPatterns.size}")
      println(s"Learning patterns: ${result.patterns.learningPatterns.size}")
      
      // Log patterns
      result.patterns.abstractionPatterns.foreach { pattern =>
        println(s"Abstraction: ${pattern.name} - ${pattern.description}")
      }
      
      result.patterns.reasoningPatterns.foreach { pattern =>
        println(s"Reasoning: ${pattern.name} - ${pattern.description}")
      }
      
      result.patterns.learningPatterns.foreach { pattern =>
        println(s"Learning: ${pattern.name} - ${pattern.description}")
      }
    }
    
    OpenCogResult(result.atomSpace, result.patterns, result.toolPlan)
  }

  /** options for OpenCog analysis */
  val options: List[PhaseOption[Config]] = List(
    ("log", BoolOption(_.log = _), "turn on logging mode."),
    ("metta", StrOption(_.mettaScript = _), "execute MeTTa script during analysis."),
    ("output", StrOption(_.outputPath = _), "output path for cognitive analysis results."),
    ("atoms-only", BoolOption(_.atomsOnly = _), "output only atom representations."),
    ("patterns-only", BoolOption(_.patternsOnly = _), "output only cognitive patterns."),
  )

  /** default configuration */
  def defaultConfig: Config = OpenCogConfig()
}

/** MeTTa script execution phase */
case object MeTTaExecute extends Phase[OpenCogResult, MeTTaResult] {
  val name = "metta-execute"
  val help = "executes MeTTa script in OpenCog context."
  
  /** phase configuration type */
  type Config = MeTTaConfig

  def apply(
    openCogResult: OpenCogResult,
    cmdConfig: CommandConfig,
    config: Config = defaultConfig,
  ): MeTTaResult = {
    val interpreter = new MeTTaInterpreter()
    val result = interpreter.execute(config.script, openCogResult.atomSpace)
    
    // Log execution trace if requested
    if (config.log) {
      println("=== MeTTa Script Execution ===")
      println(s"Script: ${config.script}")
      println("Execution trace:")
      result.executionTrace.foreach(println)
      println(s"New atoms created: ${result.newAtoms.size}")
    }
    
    result
  }

  /** options for MeTTa execution */
  val options: List[PhaseOption[Config]] = List(
    ("log", BoolOption(_.log = _), "turn on logging mode."),
    ("script", StrOption(_.script = _), "MeTTa script to execute."),
    ("output", StrOption(_.outputPath = _), "output path for MeTTa results."),
  )

  /** default configuration */
  def defaultConfig: Config = MeTTaConfig()
}

/** Tool generation phase */
case object ToolGenerate extends Phase[OpenCogResult, ToolGenerationResult] {
  val name = "tool-generate"
  val help = "generates tools based on cognitive patterns."
  
  /** phase configuration type */
  type Config = ToolGenConfig

  def apply(
    openCogResult: OpenCogResult,
    cmdConfig: CommandConfig,
    config: Config = defaultConfig,
  ): ToolGenerationResult = {
    val generator = new ToolGenerator(config)
    val tools = generator.generateTools(openCogResult.toolPlan, openCogResult.patterns)
    
    // Write tools to output if requested
    if (config.outputPath.nonEmpty) {
      tools.foreach { tool =>
        val filename = s"${config.outputPath}/${tool.name}.js"
        tool.writeToFile(filename)
      }
    }
    
    ToolGenerationResult(tools, openCogResult.toolPlan)
  }

  /** options for tool generation */
  val options: List[PhaseOption[Config]] = List(
    ("output", StrOption(_.outputPath = _), "output directory for generated tools."),
    ("format", StrOption(_.format = _), "output format (js, mjs, ts)."),
    ("template", StrOption(_.templatePath = _), "template path for tool generation."),
  )

  /** default configuration */
  def defaultConfig: Config = ToolGenConfig()
}

/** Configuration for OpenCog analysis */
case class OpenCogConfig(
  var log: Boolean = false,
  var mettaScript: String = "",
  var outputPath: String = "",
  var atomsOnly: Boolean = false,
  var patternsOnly: Boolean = false,
)

/** Configuration for MeTTa execution */
case class MeTTaConfig(
  var log: Boolean = false,
  var script: String = "",
  var outputPath: String = "",
)

/** Configuration for tool generation */
case class ToolGenConfig(
  var outputPath: String = "",
  var format: String = "js",
  var templatePath: String = "",
)

/** Result of OpenCog analysis */
case class OpenCogResult(
  atomSpace: AtomSpace,
  patterns: CognitivePatterns,
  toolPlan: ToolGenerationPlan,
) {
  override def toString: String = {
    val sb = new StringBuilder()
    sb.append(s"OpenCog Analysis Result:\n")
    sb.append(s"  AtomSpace: ${atomSpace.atoms.size} atoms\n")
    sb.append(s"  Patterns: ${patterns.abstractionPatterns.size} abstraction, ")
    sb.append(s"${patterns.reasoningPatterns.size} reasoning, ")
    sb.append(s"${patterns.learningPatterns.size} learning\n")
    sb.append(s"  Tools: ${toolPlan.abstractionTools.size + toolPlan.reasoningTools.size + toolPlan.learningTools.size} planned\n")
    sb.toString
  }
}

/** Result of tool generation */
case class ToolGenerationResult(
  tools: List[GeneratedTool],
  plan: ToolGenerationPlan,
) {
  override def toString: String = {
    val sb = new StringBuilder()
    sb.append(s"Tool Generation Result:\n")
    sb.append(s"  Generated ${tools.size} tools\n")
    tools.foreach { tool =>
      sb.append(s"  - ${tool.name}: ${tool.description}\n")
    }
    sb.toString
  }
}