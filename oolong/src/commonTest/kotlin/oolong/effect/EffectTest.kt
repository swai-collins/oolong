package oolong.effect

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oolong.Effect
import oolong.effect
import oolong.runTest
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

class EffectTest {

    sealed class ParentMsg {
        data class ChildMsgW(val childMsg: ChildMsg) : ParentMsg()
    }

    sealed class ChildMsg {
        object NoOp : ChildMsg()
    }

    @Test
    @JsName("map_should_dispatch_mapped_message")
    fun `map should dispatch mapped message`() = runTest { resolve ->
        val childEffect: Effect<ChildMsg> = { dispatch -> dispatch(ChildMsg.NoOp) }
        val parentEffect = map(childEffect) { ParentMsg.ChildMsgW(it) }
        parentEffect { msg ->
            assertEquals(msg, ParentMsg.ChildMsgW(ChildMsg.NoOp))
            resolve()
        }
    }

    @Test
    @JsName("batch_should_not_block_iteration")
    fun `batch should not block iteration`() = runTest { resolve ->
        val delay = 100L
        val range = 1..10
        val effects = batch(range.map { i ->
            effect<Int> { dispatch ->
                delay(delay)
                dispatch(i)
            }
        })
        val messages = mutableListOf<Int>()
        launch { effects { i -> messages.add(i) } }
        delay(delay * 2)
        assertEquals(range.toList(), messages.sorted())
        resolve()
    }

}
