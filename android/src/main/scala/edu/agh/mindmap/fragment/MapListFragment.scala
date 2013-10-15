package edu.agh.mindmap.fragment

import com.actionbarsherlock.app.SherlockFragment
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater}
import edu.agh.mindmap.R
import android.widget.{TextView, ListView}
import edu.agh.mindmap.components.FunctionalListAdapter
import com.michalrus.helper.ScalaFragment
import scala.util.Random

class MapListFragment extends SherlockFragment with ScalaFragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    val view = inflater.inflate(R.layout.recent_list, container, false)

    val data = Random.shuffle(
      "abc" :: "def" :: "ghi" :: "jkl" ::
      "abc" :: "def" :: "ghi" :: "jkl" ::
      "abc" :: "def" :: "ghi" :: "jkl" :: Nil).toArray

    view.find[ListView](R.id.listview).setAdapter(new FunctionalListAdapter(getActivity, R.layout.recent_list_item, data)((v, d: String) => {
      v.find[TextView](R.id.recent_list_item_name).setText(d)
      v.find[TextView](R.id.recent_list_item_detail).setText("Last mod: 1990/01/09 17:38:00")
    }))

    view
  }

}
