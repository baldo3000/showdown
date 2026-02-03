package me.baldo3000.showdown.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor

fun addInputProcessor(processor: InputProcessor) {
    if (Gdx.input.inputProcessor is InputMultiplexer) {
        (Gdx.input.inputProcessor as InputMultiplexer).addProcessor(processor)
    } else {
        Gdx.input.inputProcessor = processor
    }
}

fun removeInputProcessor(processor: InputProcessor) {
    if (Gdx.input.inputProcessor is InputMultiplexer) {
        (Gdx.input.inputProcessor as InputMultiplexer).removeProcessor(processor)
    } else if (Gdx.input.inputProcessor == processor) {
        Gdx.input.inputProcessor = null
    }
}
