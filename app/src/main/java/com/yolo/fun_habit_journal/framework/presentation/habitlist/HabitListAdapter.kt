package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.presentation.common.changeColor
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.android.synthetic.main.fragment_habit_detail.view.habit_title
import kotlinx.android.synthetic.main.layout_habit_list_item.view.habit_timestamp

class HabitListAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedHabits: LiveData<ArrayList<Habit>>,
    private val dateUtil: DateUtil
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Habit>() {

        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return HabitViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_habit_list_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            selectedHabits,
            dateUtil
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HabitViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Habit>) {
        val commitCallback = Runnable {
            // if process died must restore list position
            interaction?.restoreListPosition()
        }
        printLogD("listadapter", "size: ${list.size}")
        differ.submitList(list, commitCallback)
    }

    fun getHabit(index: Int): Habit? {
        return try {
            differ.currentList[index]
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            null
        }
    }

    class HabitViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val selectedHabits: LiveData<ArrayList<Habit>>,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(itemView) {

        private val COLOR_GREY = R.color.app_background_color
        private val COLOR_PRIMARY = R.color.colorPrimary
        private lateinit var habit: Habit

        fun bind(item: Habit) = with(itemView) {
            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, habit)
            }

            setOnLongClickListener {
                interaction?.activateMultiSelectionMode()
                interaction?.onItemSelected(adapterPosition, habit)
                true
            }

            habit = item
            habit_title.setText(item.title);
            habit_timestamp.text = dateUtil.removeTimeFromDateString(item.updated_at)

            selectedHabits.observe(lifecycleOwner, Observer { habits ->

                if (habits != null) {
                    if (habits.contains(habit)) {
                        changeColor(
                            newColor = COLOR_GREY
                        )
                    } else {
                        changeColor(
                            newColor = COLOR_PRIMARY
                        )
                    }
                } else {
                    changeColor(
                        newColor = COLOR_PRIMARY
                    )
                }
            })
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Habit)

        fun restoreListPosition()

        fun isMultiSelectionModeEnabled(): Boolean

        fun activateMultiSelectionMode()

        fun isHabitSelected(habit: Habit): Boolean
    }
}

