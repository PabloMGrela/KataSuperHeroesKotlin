package com.karumi

import android.os.Bundle
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.matchers.ToolbarMatcher.onToolbarWithTitle
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class DetailActivityTest : AcceptanceTest<SuperHeroDetailActivity>(SuperHeroDetailActivity::class.java) {

    @Mock
    lateinit var repository: SuperHeroRepository

    @Test
    fun superHeroNameIsShownAsTitle() {
        val superHero = givenThereIsASuperHero()
        startActivity(superHero)

        onToolbarWithTitle(superHero.name)
    }

    @Test
    fun superHeroNameIsShown() {
        val superHero = givenThereIsASuperHero()
        startActivity(superHero)

        onView(allOf(withId(R.id.tv_super_hero_name), withText(superHero.name))).check(matches(isDisplayed()))
    }

    @Test
    fun superHeroLoaderIsNotShown() {
        val superHero = givenThereIsASuperHero()
        startActivity(superHero)

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun superHeroDetailsIsShown() {
        val superHero = givenThereIsASuperHero()
        startActivity(superHero)

        onView(withId(R.id.tv_super_hero_description)).check(matches(isDisplayed()))
    }

    @Test
    fun superHeroAvengerLogoIsShown() {
        val superHero = givenThereIsASuperHero(true)
        startActivity(superHero)

        onView(withId(R.id.iv_avengers_badge)).check(matches(isDisplayed()))
    }

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }

    private fun givenThereIsASuperHero(isAvenger: Boolean = false): SuperHero {
        val superHeroName = "SuperHero"
        val superHeroPhoto = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg"
        val superHeroDescription = "Super Hero Description"
        val superHero = SuperHero(superHeroName, superHeroPhoto, isAvenger, superHeroDescription)
        whenever(repository.getByName(superHeroName)).thenReturn(superHero)
        return superHero
    }

    private fun startActivity(superHero: SuperHero): SuperHeroDetailActivity {
        val args = Bundle()
        args.putString("super_hero_name_key", superHero.name)
        return startActivity(args)
    }

    private fun scrollToView(viewId: Int) {
        onView(withId(viewId)).perform(scrollTo())
    }
}