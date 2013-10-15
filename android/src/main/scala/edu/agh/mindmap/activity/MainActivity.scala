package edu.agh.mindmap.activity

import scala.collection.mutable

import com.actionbarsherlock.app.SherlockFragmentActivity
import android.os.Bundle
import edu.agh.mindmap.R
import android.widget.TabHost
import android.support.v4.app.{Fragment, FragmentActivity}
import android.content.Context
import android.view.View
import edu.agh.mindmap.fragment.MapListFragment
import scala.reflect.ClassTag
import com.michalrus.helper.ScalaActivity

class MainActivity extends SherlockFragmentActivity with ScalaActivity {

  lazy val tabHost = find[TabHost](R.id.tabhost)
  lazy val tabManager = new TabManager(this, tabHost, android.R.id.tabcontent)

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    tabHost.setup()

    tabManager.addTab[MapListFragment]("all", "All maps")
    tabManager.addTab[MapListFragment]("all2", "All maps 2")
    tabManager.addTab[MapListFragment]("all3", "All maps 3")
    tabManager.addTab[MapListFragment]("all4", "All maps 4")
    tabManager.addTab[MapListFragment]("all5", "All maps 5")
    tabManager.addTab[MapListFragment]("all6", "All maps 6")
  }

  class TabManager(val activity: FragmentActivity, tabHost: TabHost, containerId: Int) extends TabHost.OnTabChangeListener {

    tabHost.setOnTabChangedListener(this)

    val creators = new mutable.HashMap[String, () => Fragment]
    val fragments = new mutable.HashMap[String, Fragment]
    var lastTabTag: Option[String] = None

    class DummyTabFactory(val context: Context) extends TabHost.TabContentFactory {
      override def createTabContent(tag: String) = {
        val v = new View(context)
        v.setMinimumWidth(0)
        v.setMinimumHeight(0)
        v
      }
    }

    def addTab[F](tag: String, label: String, args: Bundle = null)(implicit classTag: ClassTag[F]) {
      val tabSpec = tabHost.newTabSpec(tag).setIndicator(label).setContent(new DummyTabFactory(activity))
      creators += tag -> (() => Fragment.instantiate(activity, classTag.runtimeClass.getName, args))

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state.  If so, deactivate it, because our
      // initial state is that a tab isn't shown. (On orientation change.)
      Option(activity.getSupportFragmentManager.findFragmentByTag(tag)) match {
        case Some(f) => {
          if (!f.isDetached) {
            val ft = activity.getSupportFragmentManager.beginTransaction
            ft.detach(f)
            ft.commit()
          }
          fragments += tag -> f
        }
        case _ =>
      }

      tabHost.addTab(tabSpec)
    }

    override def onTabChanged(tag: String) {
      log("onTabChanged: " + tag)
      if (lastTabTag != Some(tag)) {
        val ft = activity.getSupportFragmentManager.beginTransaction
        lastTabTag match {
          case Some(t) => fragments.get(t) match {
            case Some(f) => ft.detach(f)
            case _ =>
          }
          case _ =>
        }
        (creators.get(tag), fragments.get(tag)) match {
          case (Some(creator), None) => {
            log("; creating fragment")
            val f = creator()
            fragments += tag -> f
            ft.add(containerId, f, tag)
          }
          case (_, Some(f)) => {
            log("; attaching fragment")
            ft.attach(f)
          }
          case _ =>
            log("; unknown case")
        }
        lastTabTag = Some(tag)
        ft.commit()
        activity.getSupportFragmentManager.executePendingTransactions()
      }
    }

  }

}
