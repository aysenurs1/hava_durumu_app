package com.kampplus.hava.ui.city

import com.kampplus.hava.testing.FakeCityRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Şehir listesi ekranının dört durumunu doğrular.
 *
 * Temel kural: her istek Loading ile başlar ve
 * Success / Empty / Error'dan biriyle biter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()

    private fun createViewModel() = CityViewModel(repository)

    @Test
    fun `baslangicta bos arama ekrani gosterilir`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `yazilan sehir icin otomatik arama yapilir`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.result = listOf(sampleCity())

        val viewModel = createViewModel()
        viewModel.onQueryChange("İstanbul")
        advanceUntilIdle()

        assertEquals(1, repository.callCount)
        assertEquals("İstanbul", repository.lastQuery)
    }

    @Test
    fun `veri gelince Success durumuna gecilir`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.result = listOf(sampleCity(), sampleCity(id = 2L, name = "İzmir"))

        val viewModel = createViewModel()
        viewModel.onQueryChange("Ankara")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Success bekleniyordu, gelen: $state", state is LoadState.Success)
        assertEquals(2, (state as LoadState.Success).data.size)
    }

    @Test
    fun `bos liste Empty durumuna cevrilir`() = runTest(mainDispatcherRule.testDispatcher) {
        // Cevap geldi ama içi boş: bu bir hata değildir.
        repository.result = emptyList()

        val viewModel = createViewModel()
        viewModel.onQueryChange("Ankara")
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.error = IOException("no network")

        val viewModel = createViewModel()
        viewModel.onQueryChange("Ankara")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        // Kullanıcıya teknik mesaj değil, insan dilinde bir cümle gösterilir.
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )

        val viewModel = createViewModel()
        viewModel.onQueryChange("Ankara")
        advanceUntilIdle()

        assertEquals(
            "Sunucu şu an cevap vermiyor.",
            (viewModel.uiState.value as LoadState.Error).message
        )
    }

    @Test
    fun `onQueryChange debounced arama baslatir`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Rize")
        runCurrent()
        assertEquals(0, repository.callCount)
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals("Rize", viewModel.query.value)
        assertEquals(1, repository.callCount)
        assertEquals("Rize", repository.lastQuery)
    }

    @Test
    fun `search guncel metinle tek istek atar`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        assertEquals("Rize", repository.lastQuery)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `tekrar dene yeni bir istek baslatir ve basarili olabilir`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.error = IOException("no network")
        val viewModel = createViewModel()
        viewModel.onQueryChange("Ankara")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoadState.Error)

        // Bağlantı geri geldi.
        repository.error = null
        repository.result = listOf(sampleCity())
        viewModel.search()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(2, repository.callCount)
    }
}
