package edu.agh.mindmap.activity

import scala.collection.mutable

import com.actionbarsherlock.app.SherlockFragmentActivity
import android.os.Bundle
import edu.agh.mindmap.R
import edu.agh.mindmap.helper.ScalaActivity
import android.widget.TabHost
import android.support.v4.app.{Fragment, FragmentActivity}
import android.content.Context
import android.view.View

class MainActivity extends SherlockFragmentActivity with ScalaActivity {

  lazy val tabHost = find[TabHost](R.id.tabhost)
  lazy val tabManager = new TabManager(this, tabHost, android.R.id.tabcontent)

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    tabHost.setup()
  }

  class TabManager(val activity: FragmentActivity, tabHost: TabHost, containerId: Int) extends TabHost.OnTabChangeListener {

    val creators = new mutable.HashMap[String, () => Fragment]
    val fragments = new mutable.HashMap[String, Fragment]
    var lastTabTag: Option[String] = None

    class DummyTabFactory(val context: Context) extends TabHost.TabContentFactory {

      override def createTabContent(tag: String) {
        val v = new View(context)
        v.setMinimumWidth(0)
        v.setMinimumHeight(0)
        v
      }

    }

    case class TabInfo()

    def addTab(tag: String, label: String, creator: () => Fragment) {
      val tabSpec = tabHost.newTabSpec(tag).setIndicator(label).setContent(new DummyTabFactory(activity))
      tabHost.addTab(tabSpec)
    }

    override def onTabChanged(tag: String) {
      if (lastTabTag != Some(tag)) {
        val ft = activity.getSupportFragmentManager.beginTransaction
        lastTabTag match {
          case Some(t) => fragments.get(t) match {
            case Some(f) => ft.detach(f)
            case _ =>
          }
          case _ =>
        }
      }
    }

  }

}
