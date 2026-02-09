package me.baldo3000.showdown.data

import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

@Serializable(with = Vector3DSerializer::class)
class Vector3D(x: Float = 0f, y: Float = 0f, z: Float = 0f) : Vector3(x, y, z) {
    fun to2D(): Vector2D = Vector2D(x, y)
}

object Vector3DSerializer : KSerializer<Vector3D> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.badlogic.gdx.math.Vector3") {
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
    }

    override fun serialize(encoder: Encoder, value: Vector3D) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
        }
    }

    override fun deserialize(decoder: Decoder): Vector3D {
        return decoder.decodeStructure(descriptor) {
            var x = 0f
            var y = 0f
            var z = 0f
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> x = decodeFloatElement(descriptor, 0)
                    1 -> y = decodeFloatElement(descriptor, 1)
                    2 -> z = decodeFloatElement(descriptor, 2)
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            Vector3D(x, y, z)
        }
    }
}
