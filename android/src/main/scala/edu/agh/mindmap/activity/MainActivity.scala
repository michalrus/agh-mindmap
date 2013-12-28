/*
 * Copyright 2013 Katarzyna Szawan <kat.szwn@gmail.com>
 *     and Michał Rus <https://michalrus.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.agh.mindmap.activity

import scala.collection.mutable

import com.actionbarsherlock.app.SherlockFragmentActivity
import android.os.Bundle
import edu.agh.mindmap.R
import android.widget.{HorizontalScrollView, TabHost}
import android.support.v4.app.{Fragment, FragmentActivity}
import android.content.{ActivityNotFoundException, Intent, Context}
import android.view.View
import edu.agh.mindmap.fragment.{MapFragment, MapListFragment}
import scala.reflect.ClassTag
import com.michalrus.helper.ScalaActivity
import com.actionbarsherlock.view.{MenuItem, Menu}
import com.ipaulpro.afilechooser.utils.FileUtils
import android.app.{AlertDialog, Activity}
import edu.agh.mindmap.model.{MindNode, MindMap}
import edu.agh.mindmap.util.{DBHelper, ImporterException}
import java.util.UUID
import scala.util.Try

object MainActivity {
  val FileChooserRequestCode = 31337
  val MapListTabTag = "all"
  val TabTitleMaxLength = 8
}

class MainActivity extends SherlockFragmentActivity with ScalaActivity {

  private lazy val tabHost = find[TabHost](R.id.tabhost).get // safe to throw here, application entry point, no way to deploy missing this
  private lazy val tabManager = new TabManager(this, tabHost, android.R.id.tabcontent, R.id.real_tabcontent, R.id.tab_scroll)

  // /me hates you, Android, for this:
  private var actionImport: Option[MenuItem] = None
  private var actionCreate: Option[MenuItem] = None
  private var actionClose: Option[MenuItem] = None

  override def onCreateOptionsMenu(menu: Menu) = {
    def add(id: Int, s: Int, icon: Int) = {
      val i = menu add (Menu.NONE, id, Menu.NONE, s)
      i setIcon icon setShowAsAction MenuItem.SHOW_AS_ACTION_ALWAYS
      i
    }

    actionImport = Some(add(R.id.action_import, R.string.action_import, R.drawable.icon_import))
    actionCreate = Some(add(R.id.action_create, R.string.action_create, R.drawable.icon_create))
    actionClose = Some(add(R.id.action_close, R.string.action_close, R.drawable.icon_close))

    showHideActions()

    true
  }

  def showHideActions() {
    val isMapList_? = tabHost.getCurrentTabTag == MainActivity.MapListTabTag
    actionImport foreach (_ setVisible isMapList_?)
    actionCreate foreach (_ setVisible isMapList_?)
    actionClose foreach (_ setVisible !isMapList_?)
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.action_import => showImportDialog()
      case R.id.action_create => createNewMap()
      case R.id.action_close => closeCurrentMap()
      case _ =>
    }

    true
  }

  def closeCurrentMap() {
    val tag = tabHost.getCurrentTabTag
    if (tag != MainActivity.MapListTabTag)
      tabManager removeTab (tag, tagAfterwards = MainActivity.MapListTabTag)
  }

  def createNewMap() {
    val newMap = MindMap.create
    newMap.root.content = Some(getString(R.string.default_new_map_title))
    withMapListFragment(_ addMaps newMap :: Nil )
    viewMindMap(newMap)
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

  def alertOk(title: Int, message: Int) {
    val b = new AlertDialog.Builder(this)
    b setMessage message setTitle title setCancelable false
    b setPositiveButton (R.string.button_ok, null)
    b.create.show()
  }

  def withMapListFragment(f: MapListFragment => Unit) {
    tabManager.fragments get MainActivity.MapListTabTag foreach {
      case lf: MapListFragment => f(lf)
      case _ =>
    }
  }

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    request match {
      case MainActivity.FileChooserRequestCode if result == Activity.RESULT_OK && data != null =>
        try {
          val file = FileUtils.getFile(data.getData)
          val maps = MindMap.importFrom(file) // TODO: do this asynchronously?
          withMapListFragment(_ addMaps maps)
          if (maps.nonEmpty) laterOnUiThread {
            viewMindMap(maps.head)
          }
        } catch {
          case _: ImporterException => alertOk(R.string.import_error_title, R.string.import_error_body)
          case _: Exception => alertOk(R.string.file_error_title, R.string.file_error_body)
        }
      case _ =>
    }
    super.onActivityResult(request, result, data)
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    val db = new DBHelper(this)
    MindMap setDb db
    MindNode setDb db

    setContentView(R.layout.main)
    tabHost.setup()

    tabManager.addTab[MapListFragment](MainActivity.MapListTabTag, getString(R.string.all_maps))

    Option(bundle) foreach (b => {
      Option(b getStringArray "tags") foreach(_ filter (_ != MainActivity.MapListTabTag) foreach { t =>
        MindMap findByUuid (UUID fromString t) foreach (viewMindMap(_, switchTab = false))
      })
      laterOnUiThread {
        Option(b getString "tab") foreach tabManager.focusTabOfTag
      }
    })
  }

  override def onSaveInstanceState(bundle: Bundle) {
    super.onSaveInstanceState(bundle)
    bundle putString ("tab", tabHost.getCurrentTabTag)
    bundle putStringArray ("tags", tabManager.addedTags)
  }

  override def onBackPressed() {
    tabHost.getCurrentTabTag match {
      case MainActivity.MapListTabTag => super.onBackPressed()
      case _ => tabManager focusTabOfTag MainActivity.MapListTabTag; ()
    }
  }

  def viewMindMap(map: MindMap, switchTab: Boolean = true) {
    val uuid = map.uuid.toString
    if (!tabManager.focusTabOfTag(uuid)) {
      val b = new Bundle
      b.putString("uuid", uuid)

      tabManager.addTab[MapFragment](uuid, map.root.content.getOrElse(""), b)
      if (switchTab) laterOnUiThread {
        tabManager.focusTabOfTag(uuid)
        ()
      }
    }
  }

  class TabManager(val activity: FragmentActivity with ScalaActivity, tabHost: TabHost, fakeContainerId: Int, realContainerId: Int, scrollId: Int) extends TabHost.OnTabChangeListener {

    tabHost.setOnTabChangedListener(this)

    val scrollView = activity.find[HorizontalScrollView](scrollId).get // safe to throw here, too; the app won't start at all
    val creators = new mutable.HashMap[String, () => Fragment]
    val fragments = new mutable.HashMap[String, Fragment]
    val tabSpecs = new mutable.ArrayBuffer[TabHost#TabSpec]

    var lastTabTag: Option[String] = None

    def addedTags = (tabSpecs map (_.getTag)).toArray

    class DummyTabFactory(val context: Context) extends TabHost.TabContentFactory {
      override def createTabContent(tag: String) = {
        val v = new View(context)
        v.setMinimumWidth(0)
        v.setMinimumHeight(0)
        v
      }
    }

    def addTab[F](tag: String, label: String, args: Bundle = null)(implicit classTag: ClassTag[F]) {
      val indicator = if (label.length <= MainActivity.TabTitleMaxLength) label
      else (label take MainActivity.TabTitleMaxLength) + '\u2026'

      val tabSpec = tabHost newTabSpec tag setIndicator indicator setContent new DummyTabFactory(activity)
      creators += tag -> (() => Fragment.instantiate(activity, classTag.runtimeClass.getName, args))

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state.  If so, deactivate it, because our
      // initial state is that a tab isn't shown. (On orientation change.)
      Option(activity.getSupportFragmentManager.findFragmentByTag(tag)) match {
        case Some(f) =>
          if (!f.isDetached) {
            val ft = activity.getSupportFragmentManager.beginTransaction
            ft.detach(f)
            ft.commit()
          }
          fragments += tag -> f
        case _ =>
      }

      tabSpecs += tabSpec
      tabHost addTab tabSpec
    }

    def removeTab(tag: String, tagAfterwards: String) {
      // remove unwanted TabSpec(s)
      val tabSpecsSize = tabSpecs.size
      def loop() {
        val idx = tabSpecs indexWhere (_.getTag == tag)
        if (idx > 0) {
          tabSpecs remove idx
          loop()
        }
      }
      loop()

      // if anything changed...
      if (tabSpecsSize != tabSpecs.size) {
        // recreate tab view(s)
        val scrollX = tabHost.getScrollX
        tabHost clearAllTabs()
        tabSpecs foreach (s => tabHost addTab s)
        tabHost setScrollX scrollX

        // switch to another tab
        focusTabOfTag(tagAfterwards)
      }

      // update creators map
      creators -= tag

      // update fragments map, remove the fragment
      fragments get tag match {
        case Some(fragment) =>
          val ft = activity.getSupportFragmentManager.beginTransaction
          ft remove fragment
          ft commit()

          fragments -= tag
          ()
        case _ =>
      }
    }

    def focusTabOfTag(tag: String): Boolean = {
      creators get tag match {
        case Some(_) =>
          val sX = tabHost.getScrollX
          tabHost setCurrentTabByTag tag
          tabHost setScrollX sX // setCurrentTabByTag scrolls without anim, that's why
          true
        case _ => false
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
          case (Some(creator), None) =>
            val f = creator()
            fragments += tag -> f
            ft.add(realContainerId, f, tag)
          case (_, Some(f)) => if (f.isHidden) ft.show(f) else ft.attach(f)
          case _ =>
        }
        lastTabTag = Some(tag)
        Try { // might throw if this gets called after activity is destroyed... Android. :(
          ft.commit()
          activity.getSupportFragmentManager.executePendingTransactions()
        }
        laterOnUiThread {
          rescrollTabView()
        }

        showHideActions()
      }
    }

    def rescrollTabView() {
      val tv = tabHost.getCurrentTabView
      val scroll = tv.getLeft + tv.getWidth / 2 - scrollView.getWidth / 2
      scrollView.smoothScrollTo(scroll, 0)
    }

  }

}
