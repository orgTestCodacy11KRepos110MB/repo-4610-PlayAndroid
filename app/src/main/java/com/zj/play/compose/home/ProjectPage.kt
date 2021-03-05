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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zj.model.pojo.QueryArticle
import com.zj.model.room.entity.Article
import com.zj.model.room.entity.ProjectClassify
import com.zj.play.R
import com.zj.play.compose.common.ArticleItem
import com.zj.play.compose.common.ErrorContent
import com.zj.play.compose.common.LoadingContent
import com.zj.play.compose.model.PlayError
import com.zj.play.compose.model.PlayLoading
import com.zj.play.compose.model.PlaySuccess
import com.zj.play.project.ProjectViewModel
import com.zj.play.project.list.ProjectListViewModel
import dev.chrisbanes.accompanist.insets.statusBarsHeight

@Composable
fun ProjectPage(
    enterArticle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel = viewModel(),
    projectViewModel: ProjectListViewModel = viewModel()
) {
    val onRefreshPostsState by rememberUpdatedState(0)
    if (onRefreshPostsState == 0) {
        viewModel.getDataList(false)
    }
    val result by viewModel.dataLiveData.observeAsState()
    val position = viewModel.position.observeAsState()
    val articleList by projectViewModel.dataLiveData.observeAsState()
    when (result) {
        is PlayLoading -> {
            LoadingContent()
        }
        is PlaySuccess<*> -> {
            onRefreshPostsState.and(1)
            val data = result as PlaySuccess<List<ProjectClassify>>
            if (position.value == 0) {
                projectViewModel.getDataList(
                    QueryArticle(
                        0,
                        data.data[0].id,
                        true
                    )
                )
            }
            Column {
                Column(modifier = Modifier.background(color = colorResource(id = R.color.yellow))) {
                    Spacer(modifier = Modifier.statusBarsHeight())
                    ScrollableTabRow(
                        selectedTabIndex = position.value ?: 0,
                        modifier = Modifier.wrapContentWidth(),
                        edgePadding = 3.dp
                    ) {
                        data.data.forEachIndexed { index, projectClassify ->
                            Tab(
                                text = { Text(projectClassify.name) },
                                selected = position.value == index,
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
                    }
                }
                when (articleList) {
                    is PlayLoading -> {
                        LoadingContent()
                    }
                    is PlaySuccess<*> -> {
                        val articles = articleList as PlaySuccess<List<Article>>
                        LazyColumn(modifier) {
                            if (articleList == null) {
                                item {
                                    ErrorContent(enterArticle = { /*TODO*/ })
                                }
                                return@LazyColumn
                            }
                            itemsIndexed(articles.data) { index, article ->
                                ArticleItem(article, index, enterArticle)
                            }
                        }
                    }
                    is PlayError -> ErrorContent(enterArticle = { })
                }

            }
        }
        is PlayError -> {
            ErrorContent(enterArticle = { })
        }
    }

}