package android.support.v4.app

//	Fix SecurityException on android O
abstract class MyJobIntentService : JobIntentService() {

    internal override fun dequeueWork(): JobIntentService.GenericWorkItem? {
        try {
            return super.dequeueWork()
        } catch (ignored: SecurityException) {
            // intentionally left blank
        }

        return null
    }
}