package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import androidx.lifecycle.LiveData
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.usecases.habitdetail.HabitDetailInteractors
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.UPDATE_HABIT_FAILED
import com.yolo.fun_habit_journal.framework.datasource.cache.model.HabitCacheEntity
import com.yolo.fun_habit_journal.framework.presentation.common.BaseViewModel
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.CollapsingToolbarState
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailViewState
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitInteractionManager
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitInteractionState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


const val HABIT_DETAIL_ERROR_RETRIEVEING_SELECTED_HABIT = "Error retrieving selected habit from bundle."
@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailViewModel
@Inject
constructor(
    private val habitDetailInteractors: HabitDetailInteractors
) : BaseViewModel<HabitDetailViewState>() {

    private val habitInteractionManager: HabitInteractionManager = HabitInteractionManager()
    val habitTitleInteractionState: LiveData<HabitInteractionState>
        get() = habitInteractionManager.habitTitleState
    val habitBodyInteractionState: LiveData<HabitInteractionState>
        get() = habitInteractionManager.noteBodyState
    val collapsingToolbarState: LiveData<CollapsingToolbarState>
        get() = habitInteractionManager.collapsingToolbarState

    override fun handleNewData(data: HabitDetailViewState) {
        // no data coming in from requests...
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<HabitDetailViewState>?> = when (stateEvent) {

            is HabitDetailStateEvent.UpdateHabitEvent -> {
                val id = getHabit()?.id
                if (!isHabitTitleNull() && id != null) {
                    habitDetailInteractors.updateHabitUseCase.updateHabit(
                        habit = getHabit()!!,
                        stateEvent = stateEvent
                    )
                } else {
                    emitStateMessageEvent(
                        stateMessage = StateMessage(
                            response = Response(
                                message = UPDATE_HABIT_FAILED,
                                uiComponentType = UIComponentType.Dialog,
                                messageType = MessageType.Error
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }

            is HabitListStateEvent.DeleteHabitEvent -> {
                habitDetailInteractors.deleteHabitUseCase.deleteHabit(
                    habit = stateEvent.habit,
                    stateEvent = stateEvent
                )
            }

            is HabitDetailStateEvent.CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    fun beginPendingDelete(habit: Habit) {
        setStateEvent(
            HabitListStateEvent.DeleteHabitEvent(
                habit = habit
            )
        )
    }

    private fun isHabitTitleNull(): Boolean {
        val title = getHabit()?.title
        if (title.isNullOrBlank()) {
            setStateEvent(
                HabitDetailStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = HABIT_TITLE_CANNOT_BE_EMPTY,
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Info
                        )
                    )
                )
            )
            return true
        } else {
            return false
        }
    }

    fun getHabit(): Habit? {
        return getCurrentViewStateOrNew().habit
    }

    override fun initNewViewState(): HabitDetailViewState {
        return HabitDetailViewState()
    }

    fun setHabit(habit: Habit?) {
        val update = getCurrentViewStateOrNew()
        update.habit = habit
        setViewState(update)
    }

    fun setCollapsingToolbarState(
        state: CollapsingToolbarState
    ) = habitInteractionManager.setCollapsingToolbarState(state)

    fun updateNote(title: String?, body: String?) {
        updateHabitTitle(title)
        updateHabitBody(body)
    }

    fun updateHabitTitle(title: String?) {
        if (title == null) {
            setStateEvent(
                HabitDetailStateEvent.CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = HabitCacheEntity.nullTitleError(),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        )
                    )
                )
            )
        } else {
            val update = getCurrentViewStateOrNew()
            val updatedNote = update.habit?.copy(
                title = title
            )
            update.habit = updatedNote
            setViewState(update)
        }
    }

    fun updateHabitBody(body: String?) {
        val update = getCurrentViewStateOrNew()
        val updatedNote = update.habit?.copy(
            body = body ?: ""
        )
        update.habit = updatedNote
        setViewState(update)
    }

    fun setNoteInteractionTitleState(state: HabitInteractionState) {
        habitInteractionManager.setNewNoteTitleState(state)
    }

    fun setNoteInteractionBodyState(state: HabitInteractionState) {
        habitInteractionManager.setNewNoteBodyState(state)
    }

    fun isToolbarCollapsed() = collapsingToolbarState.toString()
        .equals(CollapsingToolbarState.Collapsed().toString())

    fun setIsUpdatePending(isPending: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isUpdatePending = isPending
        setViewState(update)
    }

    fun getIsUpdatePending(): Boolean {
        return getCurrentViewStateOrNew().isUpdatePending ?: false
    }

    fun isToolbarExpanded() = collapsingToolbarState.toString()
        .equals(CollapsingToolbarState.Expanded().toString())

    fun checkEditState() = habitInteractionManager.checkEditState()

    fun exitEditState() = habitInteractionManager.exitEditState()

    fun isEditingTitle() = habitInteractionManager.isEditingTitle()

    fun isEditingBody() = habitInteractionManager.isEditingBody()

    // force observers to refresh
    fun triggerHabitObservers() {
        getCurrentViewStateOrNew().habit?.let { habit ->
            setHabit(habit)
        }
    }
}
