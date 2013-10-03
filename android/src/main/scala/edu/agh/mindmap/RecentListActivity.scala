package edu.agh.mindmap

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class RecentListActivity extends Activity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.recent_list)
    findViewById(R.id.textview).asInstanceOf[TextView].setText("hello, deer!")
  }
}
