package edu.agh.mindmap.model

import java.util.UUID

class MindNode(val map: MindMap,
               val uuid: UUID,
               var parent: Option[MindNode],
               var content: Option[String],
               var hasConflict: Boolean,
               var cloudTime: Option[Long])
