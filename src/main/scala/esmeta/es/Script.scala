package esmeta.es

import esmeta.spec.*
import esmeta.spec.BuiltinPath.*
import esmeta.util.*
import esmeta.util.Appender.*

/** ECMAScript script program */
case class Script(code: String, name: String) extends ESElem
