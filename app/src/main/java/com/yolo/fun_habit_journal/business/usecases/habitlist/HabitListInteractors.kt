package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.usecases.common.usecase.DeleteHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.DeleteMultipleHabitsUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GetHabitsCountUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.InsertNewHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.RestoreDeletedHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.SearchHabitsUseCase
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState

class HabitListInteractors(
    val insertNewHabitUseCase: InsertNewHabitUseCase,
    val deleteHabitUseCase: DeleteHabitUseCase<HabitListViewState>,
    val searchHabitsUseCase: SearchHabitsUseCase,
    val getHabitsCountUseCase: GetHabitsCountUseCase,
    val restoreDeletedHabitUseCase: RestoreDeletedHabitUseCase,
    val deleteMultipleHabitsUseCase: DeleteMultipleHabitsUseCase
)
