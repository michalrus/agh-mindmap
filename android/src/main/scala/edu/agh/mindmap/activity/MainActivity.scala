package edu.agh.mindmap.activity

import scala.collection.mutable

import com.actionbarsherlock.app.SherlockFragmentActivity
import android.os.Bundle
import edu.agh.mindmap.R
import android.widget.{HorizontalScrollView, TabHost}
import android.support.v4.app.{Fragment, FragmentActivity}
import android.content.Context
import android.view.View
import edu.agh.mindmap.fragment.MapListFragment
import scala.reflect.ClassTag
import com.michalrus.helper.ScalaActivity
import com.actionbarsherlock.view.{MenuItem, Menu}

class MainActivity extends SherlockFragmentActivity with ScalaActivity {

  lazy val tabHost = find[TabHost](R.id.tabhost)
  lazy val tabManager = new TabManager(this, tabHost, android.R.id.tabcontent, R.id.tab_scroll)

  override def onCreateOptionsMenu(menu: Menu) = {
    def add(id: Int, s: Int, icon: Int) =
      menu.add(Menu.NONE, id, Menu.NONE, s).
        setIcon(icon).
        setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    add(R.id.action_import, R.string.action_import, R.drawable.icon_import)
    add(R.id.action_create, R.string.action_create, R.drawable.icon_create)

    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.action_import => log("import")
      case R.id.action_create => log("create")
      case _ =>
    }

    true
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    tabHost.setup()

    tabManager.addTab[MapListFragment]("all", "All maps")

    Option(bundle) foreach (b => {
      tabHost.setCurrentTabByTag(b.getString("tab"))
      laterOnUiThread {
        tabManager.rescrollTabView()
      }
    })
  }

  override def onSaveInstanceState(bundle: Bundle) {
    super.onSaveInstanceState(bundle)
    bundle.putString("tab", tabHost.getCurrentTabTag)
  }

  class TabManager(val activity: FragmentActivity with ScalaActivity, tabHost: TabHost, containerId: Int, scrollId: Int) extends TabHost.OnTabChangeListener {

    tabHost.setOnTabChangedListener(this)

    val scrollView = activity.find[HorizontalScrollView](scrollId)
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
      if (lastTabTag != Some(tag)) {
        val ft = activity.getSupportFragmentManager.beginTransaction
        lastTabTag match {
          case Some(t) => fragments.get(t) match {
            case Some(f) => ft.hide(f)
            case _ =>
          }
          case _ =>
        }
        (creators.get(tag), fragments.get(tag)) match {
          case (Some(creator), None) => {
            val f = creator()
            fragments += tag -> f
            ft.add(containerId, f, tag)
          }
          case (_, Some(f)) => if (f.isHidden) ft.show(f) else ft.attach(f)
          case _ =>
        }
        lastTabTag = Some(tag)
        ft.commit()
        activity.getSupportFragmentManager.executePendingTransactions()
        rescrollTabView()
      }
    }

    def rescrollTabView() {
      val tv = tabHost.getCurrentTabView
      val scroll = tv.getLeft + tv.getWidth / 2 - scrollView.getWidth / 2
      scrollView.smoothScrollTo(scroll, 0)
    }

  }

}
