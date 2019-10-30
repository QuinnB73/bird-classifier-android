package honours.project.bird_classifier.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import honours.project.bird_classifier.R

class BirdArrayAdapter(context: Context, @LayoutRes private val layoutRes: Int,
                       private val birdList: MutableList<Bird>)
    : ArrayAdapter<Bird>(context, layoutRes, birdList) {

    class ViewHolder {
        var textView: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder: ViewHolder?
        var actualView: View? = convertView

        if (actualView == null) {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            actualView = inflater.inflate(layoutRes, parent, false)

            holder = ViewHolder()
            holder.textView = actualView.findViewById(R.id.bird_list_item_tv)

            actualView.tag = holder
        } else {
            holder = actualView.tag as ViewHolder
        }

        holder.textView?.text = birdList[position].displayName

        return actualView!!
    }
}