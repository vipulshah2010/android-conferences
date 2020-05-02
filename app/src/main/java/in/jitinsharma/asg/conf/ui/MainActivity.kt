package `in`.jitinsharma.asg.conf.ui

import `in`.jitinsharma.asg.conf.database.AppDatabase
import `in`.jitinsharma.asg.conf.repository.ConferenceRepository
import `in`.jitinsharma.asg.conf.viewmodel.*
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.animation.Crossfade
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.livedata.observeAsState
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val conferenceViewModel: ConferenceViewModel by viewModels {
        ConferenceViewModelFactory(
            ConferenceRepository(AppDatabase.getDatabase(applicationContext))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conferenceViewModel.loadConferenceList()
        setContent {
            val conferenceDataListState =
                conferenceViewModel.conferenceListLiveData.observeAsState()
            MaterialTheme(colors = themeColors) {
                Box(backgroundColor = MaterialTheme.colors.primary) {
                    val filtersScreenState = FiltersScreenState(shouldDisplay = false)
                    Column(modifier = Modifier.fillMaxWidth().padding(all = 16.dp)) {
                        Header(
                            modifier = Modifier.padding(bottom = 16.dp),
                            onFilterIconClick = {
                                filtersScreenState.shouldDisplay =
                                    filtersScreenState.shouldDisplay.not()
                            })
                        Crossfade(current = conferenceDataListState.value) { state ->
                            when (state) {
                                is LoadingState -> {
                                    LoadingView()
                                }
                                is SuccessState -> {
                                    ConferenceCardList(conferenceDataList = state.conferenceDataList)
                                    FilterDialog(
                                        filtersScreenState = filtersScreenState,
                                        onApplyClick = { filter ->
                                            conferenceViewModel.filterList(filter = filter)
                                        },
                                        conferenceViewModel = conferenceViewModel
                                    )
                                }
                                is ErrorState -> {
                                    WtfView(
                                        onRetryClick = { conferenceViewModel.loadConferenceList() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

