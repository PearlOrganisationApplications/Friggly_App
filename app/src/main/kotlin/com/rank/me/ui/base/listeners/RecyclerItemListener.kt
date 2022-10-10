package com.rank.me.ui.base.listeners

import com.rank.me.data.dto.recipes.RecipesItem

/**
 * Created by Saurabh, 27th sept 2022
 */

interface RecyclerItemListener {
    fun onItemSelected(recipe: RecipesItem)
}
