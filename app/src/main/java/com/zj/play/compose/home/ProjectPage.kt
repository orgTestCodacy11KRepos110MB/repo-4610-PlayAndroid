/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zj.play.compose.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zj.play.compose.model.QueryArticle
import com.zj.model.room.entity.Article
import com.zj.model.room.entity.ProjectClassify
import com.zj.play.compose.common.article.ToTopButton
import com.zj.play.compose.common.lce.*
import com.zj.play.compose.model.PlayLoading
import com.zj.play.compose.model.PlaySuccess
import com.zj.play.compose.viewmodel.*
import dev.chrisbanes.accompanist.insets.statusBarsHeight

private const val TAG = "ProjectPage"

@Composable
fun ProjectPage(
    enterArticle: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProjectViewModel = viewModel()
    val projectViewModel: ProjectListViewModel = viewModel()
    ArticleListPage(
        modifier = modifier,
        enterArticle = enterArticle,
        viewModel = viewModel,
        projectViewModel = projectViewModel
    )
}


@Composable
fun ArticleListPage(
    modifier: Modifier,
    enterArticle: (Article) -> Unit,
    viewModel: BaseAndroidViewModel<Boolean>,
    projectViewModel: BaseAndroidViewModel<QueryArticle>
) {

    val listState = rememberLazyListState()
    var loadState by remember { mutableStateOf(false) }
    var loadPageState by remember { mutableStateOf(false) }
    val refresh by viewModel.refreshState.observeAsState()
    val loadRefresh by viewModel.loadRefreshState.observeAsState()

    if (!loadState && refresh != REFRESH_START && loadRefresh != REFRESH_START) {
        viewModel.getDataList(false)
    }
    val result by viewModel.dataLiveData.observeAsState(PlayLoading)
    val position by viewModel.position.observeAsState()

    Column(modifier = Modifier.background(color = MaterialTheme.colors.primary)) {
        Spacer(modifier = Modifier.statusBarsHeight())
        SetLcePage(playState = result, onErrorClick = {
            viewModel.getDataList(false)
            loadState = true
        }) {
            loadState = true
            val data = (result as PlaySuccess<List<ProjectClassify>>).data
            ArticleTabRow(data, loadPageState, position, viewModel, projectViewModel)
            ArticleSwipeList(
                modifier,
                data,
                viewModel,
                projectViewModel,
                position,
                listState,
                enterArticle,
            ) {
                loadPageState = true
            }
        }
    }
    ToTopButton(listState)

}

@Composable
fun ArticleSwipeList(
    modifier: Modifier,
    data: List<ProjectClassify>,
    viewModel: BaseAndroidViewModel<Boolean>,
    projectViewModel: BaseAndroidViewModel<QueryArticle>,
    position: Int?,
    listState: LazyListState,
    enterArticle: (Article) -> Unit,
    onErrorClick: () -> Unit,
) {
    SwipeArticleListPage(
        modifier = modifier,
        viewModel = projectViewModel,
        listState = listState,
        enterArticle = enterArticle,
        onRefresh = {
            viewModel.onPageChanged(0)
            projectViewModel.onRefreshChanged(REFRESH_START)
            Log.e(TAG, "onRefresh: 开始刷新")
            projectViewModel.getDataList(
                QueryArticle(
                    0,
                    data[position ?: 0].id,
                    false
                )
            )
        },
        onLoad = {
            viewModel.onPageChanged((viewModel.page.value ?: 1) + 1)
            projectViewModel.onLoadRefreshStateChanged(REFRESH_START)
            projectViewModel.getDataList(
                QueryArticle(
                    viewModel.page.value ?: 0,
                    data[position ?: 0].id,
                    true
                )
            )
        },
        onErrorClick = {
            viewModel.onLoadRefreshStateChanged(REFRESH_STOP)
            viewModel.onRefreshChanged(REFRESH_STOP)
            projectViewModel.getDataList(
                QueryArticle(
                    0,
                    data[position ?: 0].id,
                    true
                )
            )
            onErrorClick()
        }) {}
}

@Composable
fun ArticleTabRow(
    data: List<ProjectClassify>,
    loadPageState: Boolean,
    position: Int?,
    viewModel: BaseAndroidViewModel<Boolean>,
    projectViewModel: BaseAndroidViewModel<QueryArticle>
) {
    ScrollableTabRow(
        selectedTabIndex = position ?: 0,
        modifier = Modifier.wrapContentWidth(),
        edgePadding = 3.dp
    ) {
        data.forEachIndexed { index, projectClassify ->
            Tab(
                text = { Text(projectClassify.name) },
                selected = position == index,
                onClick = {
                    projectViewModel.getDataList(
                        QueryArticle(
                            0,
                            projectClassify.id,
                            true
                        )
                    )
                    viewModel.onPositionChanged(index)
                }
            )
        }

        if (position == 0 && !loadPageState) {
            projectViewModel.getDataList(
                QueryArticle(
                    projectViewModel.page.value ?: 0,
                    data[0].id,
                    true
                )
            )
        }
    }
}

