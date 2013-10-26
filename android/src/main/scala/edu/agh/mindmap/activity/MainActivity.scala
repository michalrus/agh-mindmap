package edu.agh.mindmap.activity

import scala.collection.mutable

import com.actionbarsherlock.app.SherlockFragmentActivity
import android.os.Bundle
import edu.agh.mindmap.R
import android.widget.{HorizontalScrollView, TabHost}
import android.support.v4.app.{Fragment, FragmentActivity}
import android.content.{ActivityNotFoundException, Intent, Context}
import android.view.View
import edu.agh.mindmap.fragment.MapListFragment
import scala.reflect.ClassTag
import com.michalrus.helper.ScalaActivity
import com.actionbarsherlock.view.{MenuItem, Menu}
import com.ipaulpro.afilechooser.utils.FileUtils
import android.app.Activity
import edu.agh.mindmap.model.MindMap
import edu.agh.mindmap.util.ImporterException

object MainActivity {
  val FileChooserRequestCode = 31337
}

class MainActivity extends SherlockFragmentActivity with ScalaActivity {

  private lazy val tabHost = find[TabHost](R.id.tabhost)
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
      case R.id.action_import => showImportDialog()
      case R.id.action_create => log("create")
      case _ =>
    }

    true
  }

  def showImportDialog() {
    val target = FileUtils.createGetContentIntent
    val intent = Intent.createChooser(target, getString(R.string.choose_xmind))
    try {
      startActivityForResult(intent, MainActivity.FileChooserRequestCode)
    } catch {
      case _: ActivityNotFoundException =>
    }
  }

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    request match {
      case MainActivity.FileChooserRequestCode if result == Activity.RESULT_OK && data != null =>
        try {
          val file = FileUtils.getFile(data.getData)
          // TODO:
          val map = MindMap.importFrom(file)
          // TODO: open a new tab with this map
        } catch {
          case _: ImporterException => // TODO: alert
          case _: Exception => // TODO: alert (invalid file)
        }
      case _ =>
    }
    super.onActivityResult(request, result, data)
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    tabHost.setup()

    tabManager.addTab[MapListFragment]("all", "All maps")

    Option(bundle) foreach (b => {
      tabHost.setCurrentTabByTag(b.getString("tab"))
      tabManager.rescrollTabView()
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

    def focusTabOfTag(tag: String): Boolean = {
      log("focusTabOfTag " + tag)
      creators get tag match {
        case Some(_) =>
          log("  found")
          tabHost setCurrentTabByTag tag
          rescrollTabView()
          true
        case _ =>
          log("  not found")
          false
      }
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

    def rescrollTabView() = laterOnUiThread {
      val tv = tabHost.getCurrentTabView
      val scroll = tv.getLeft + tv.getWidth / 2 - scrollView.getWidth / 2
      scrollView.smoothScrollTo(scroll, 0)
    }

  }

}
