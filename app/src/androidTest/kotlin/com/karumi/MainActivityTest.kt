package com.karumi

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.matchers.ToolbarMatcher.onToolbarWithTitle
import com.karumi.recyclerview.RecyclerViewInteraction
import com.karumi.ui.view.MainActivity
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class MainActivityTest : AcceptanceTest<MainActivity>(MainActivity::class.java) {
    companion object {
        private const val ANY_NUMBER_OF_SUPER_HEROES = 10
    }

    @Mock
    lateinit var repository: SuperHeroRepository

    @Test
    fun showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes()

        startActivity()

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()))
    }

    @Test
    fun hideTheEmptyCaseIfThereAreSuperHeroes() {
        givenThereAreSomeSuperHeroes(3)

        startActivity()

        onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())))
    }

    @Test
    fun showsScreenTitle() {
        startActivity()

        onToolbarWithTitle("Kata Screenshot")
    }

    @Test
    fun showsAvengerBadgeIfSuperHeroeIsAvenger() {
        givenThereAreSomeSuperHeroes(10, true)
        val superHeroes = repository.getAllSuperHeroes()

        startActivity()

        checkIconVisibility(superHeroes, true)
    }

    @Test
    fun showsAvengerBadgeIfSuperHeroeIsNotAvenger() {
        givenThereAreSomeSuperHeroes(10, false)
        val superHeroes = repository.getAllSuperHeroes()

        startActivity()

        checkIconVisibility(superHeroes, false)
    }

    @Test
    fun checkNavigationToDetailScreen() {
        val superHeroes = givenThereAreSomeSuperHeroes(10, true)
        val superHeroIndex = 0
        startActivity()

        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(superHeroIndex, click()))
        val superHeroSelected = superHeroes[superHeroIndex]

        intended(IntentMatchers.hasComponent(SuperHeroDetailActivity::class.java.canonicalName))
        intended(IntentMatchers.hasExtra("super_hero_name_key", superHeroSelected.name))
    }

    @Test
    fun checkLoadingWhileSuperheroesAreLoading() {
        val superHeroes = givenThereAreSomeSuperHeroes(10, true)

        whenever(repository.getAllSuperHeroes()).thenAnswer {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
            }
        }
        startActivity()
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }

    private fun checkIconVisibility(superHeroes: List<SuperHero>, visibility: Boolean) {
        val visible = if (visibility) Visibility.VISIBLE else Visibility.GONE
        RecyclerViewInteraction.onRecyclerView<SuperHero>(withId(R.id.recycler_view))
            .withItems(superHeroes)
            .check { _, view, exception ->
                matches(
                    hasDescendant(
                        Matchers.allOf<View>(
                            withId(R.id.iv_avengers_badge),
                            withEffectiveVisibility(visible)
                        )
                    )
                ).check(
                    view,
                    exception
                )
            }
    }

    private fun givenThereAreSomeSuperHeroes(
        numberOfSuperHeroes: Int = ANY_NUMBER_OF_SUPER_HEROES,
        avengers: Boolean = false
    ): List<SuperHero> {
        val superHeroes = IntRange(0, numberOfSuperHeroes - 1).map {
            val superHeroName = "SuperHero - $it"
            val superHeroPhoto = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg"
            val superHeroDescription = "Description Super Hero - $it"
            val superHero = SuperHero(superHeroName, superHeroPhoto, avengers, superHeroDescription)
            superHero
        }

        superHeroes.forEach { whenever(repository.getByName(it.name)).thenReturn(it) }
        whenever(repository.getAllSuperHeroes()).thenReturn(superHeroes)
        return superHeroes
    }

    private fun givenThereAreNoSuperHeroes() {
        whenever(repository.getAllSuperHeroes()).thenReturn(emptyList())
    }

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }
}