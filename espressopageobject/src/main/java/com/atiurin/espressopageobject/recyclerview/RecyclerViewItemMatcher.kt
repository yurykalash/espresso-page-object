package com.atiurin.espressopageobject.recyclerview

import android.support.test.espresso.util.TreeIterables
import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

open class RecyclerViewItemMatcher(val recyclerViewMatcher: Matcher<View>) {
    var recyclerView: RecyclerView? = null
    open fun atItem(itemMatcher: Matcher<View>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var itemView: View? = null
            override fun describeTo(description: Description?) {
                if (recyclerView == null) {
                    description?.appendText("No matching recycler view with : [$recyclerViewMatcher]. ")
                    return
                }
                description?.appendText("Found recycler view matches : [$recyclerViewMatcher]. ")
                if (itemView == null) {
                    description?.appendText("No matching recycler view item with : [$itemMatcher]")
                    return
                }
            }

            override fun matchesSafely(view: View?): Boolean {
                itemView = findItemView(itemMatcher, view?.rootView)
                return if (itemView != null) {
                    itemView == view
                } else false
            }
        }
    }

    open fun atItemChild(itemMatcher: Matcher<View>, childMatcher: Matcher<View>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var itemView: View? = null
            var childView: View? = null
            override fun describeTo(description: Description?) {
                if (recyclerView == null) {
                    description?.appendText("No matching recycler view with : [$recyclerViewMatcher]. ")
                    return
                }
                description?.appendText("Found recycler view matches : [$recyclerViewMatcher]. ")
                if (itemView == null) {
                    description?.appendText("No matching recycler view item with : [$itemMatcher]")
                    return
                }
                description?.appendText("Found recycler view item matches : [$itemMatcher]. ")
                if (childView == null) {
                    description?.appendText("No matching item child view with : [$childMatcher]")
                    return
                }
            }

            override fun matchesSafely(view: View?): Boolean {
                itemView = findItemView(itemMatcher, view?.rootView)
                if (itemView != null) {
                    for (childView in TreeIterables.breadthFirstViewTraversal(itemView)) {
                        if (childMatcher.matches(childView)) {
                            this.childView = childView
                            break
                        }
                    }
                }
                return if (childView != null) {
                    childView == view
                } else false
            }
        }
    }

    private fun findItemView(itemMatcher: Matcher<View>, rootView: View?): View? {
        for (childView in TreeIterables.breadthFirstViewTraversal(rootView)) {
            if (recyclerViewMatcher.matches(childView)) {
                val recyclerView = childView as RecyclerView
                this.recyclerView = recyclerView    // to describe the error
                val viewHolderMatcher: Matcher<RecyclerView.ViewHolder> =
                    viewHolderMatcher(itemMatcher)
                val matchedItems: List<MatchedItem> =
                    itemsMatching(recyclerView, viewHolderMatcher, 1)
                if (matchedItems.isEmpty()) return null
                return recyclerView.findViewHolderForAdapterPosition(matchedItems[0].position)?.itemView
            }
        }
        return null
    }
}
