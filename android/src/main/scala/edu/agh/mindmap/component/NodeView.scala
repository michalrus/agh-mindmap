package edu.agh.mindmap.component

import android.widget.{Button, EditText, FrameLayout}
import android.content.Context
import android.util.AttributeSet
import edu.agh.mindmap.R
import com.michalrus.helper.ViewHelperWithoutContext

class NodeView(context: Context, attrs: AttributeSet)
  extends FrameLayout(context, attrs) with ViewHelperWithoutContext {

  // Ad. `Option#get`: safe to throw here, crucial functionality
  lazy val content = this.find[EditText](R.id.content).get
  lazy val addButton = this.find[Button](R.id.add_button).get

}
