package cl.elgnocci.mascota

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

/** Pantalla provisoria hasta la Tarea 6. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).apply {
                text = getString(R.string.app_name)
                textSize = 32f
                gravity = Gravity.CENTER
            }
        )
    }
}
