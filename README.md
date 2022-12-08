# RFRL Android SDK
![Version](https://img.shields.io/github/v/tag/rfrl-ai/android-sdk?style=plastic&label=version)

### Setup

SDK library is published to our maven repository and can be added via Gradle:

```kotlin
// Project level build.gradle.kts
allprojects {
  repositories {
    maven("https://rfrl-builds.s3.eu-central-1.amazonaws.com/saas/repository/")
  }
}

// App level build.gradle.kts
dependencies {
  // ":sdk-dev" provides debug interface
  //implementation("ai.rfrl.saas:sdk-dev:${rfrl_version}")
  implementation("ai.rfrl.saas:sdk:${rfrl_version}")
}
```

Also, you must [enable view binding](https://github.com/rfrl-ai/android-sdk/blob/master/app/build.gradle.kts#L45) in your project.

You may find an [example of SDK setup on GitHub](https://github.com/rfrl-ai/android-sdk/blob/master/app/src/main/kotlin/ai/rfrl/saas/test/SdkRunner.kt#L61).

### Initialization

SDK must be [initialized](#initconfig-rfrlinitconfig-completecallback-rfrlinitcallback) before using:

```kotlin
val config = RfrlInitConfig(context, "sdk_client_token")
RfrlSdk.init(config) { result ->
...
}
```

You must specify Android Context and SDK client token.

*Every call to RFRL SDK must be done from main application thread.*

## RfrlSdk

Singletone factory to produce [RfrlContext](#RfrlContext) objects.

### init(config: [RfrlInitConfig](#RfrlInitConfig), completeCallback: [RfrlInitCallback](#rfrlinitcallback-rfrlinitresult-rfrlinitstatus))

Initializes SDK. Must be completed before any further call to SDK.

Arguments:

* config: [RfrlInitConfig](#RfrlInitConfig) — SDK configuration.
* completeCallback: [RfrlInitCallback](#rfrlinitcallback-rfrlinitresult-rfrlinitstatus) — completion callback.

### createContext(): [RfrlContext](#RfrlContext)

Creates an instance of RfrlContext object.

### getDebugInterface(): RfrlDebug

Obtains debug interface (for sdk-dev builds only).

### isSegmentationSupported(): Boolean

Returns true, if the current user device supports image segmentation.

### getVersion(): String

Returns RFRL SDK version string. For example: `1.2.30`.

### muteErrorMessages: Boolean

Setting this field to `true` will mute all error toasts displayed by SDK.

## RfrlContext

RfrlContext is the current SDK session. It contains necessary information to process, recognize and moderate images and video made by user.

### startCamera(activity: Activity, resultCallback: [RfrlCameraCallback](#RfrlCameraCallback))

Launches camera screen. The user can take a selfie or choose a photo from the device Gallery.

Arguments:

* activity — Android activity, which will start SDK camera activity.
* resultCallback: [RfrlCameraCallback](#RfrlCameraCallback) — callback called, when SDK camera activity is closed.

### closeCameraActivity()

Force closes camera activity.

### release()

Releases session resources. This RfrlContext instance can not be used anymore.

### setOriginal(bitmap: Bitmap)

Sets Bitmap as an original image, as if the user took selfie of selected photo from the device Gallery.

This will reset context inner state.

### startImageRecognition(resultCallback: [RfrlRecognitionCallback](#RfrlRecognitionCallback))

Starts image recognition on our servers. You must set original image first with [camera](#startcameraactivity-activity-resultcallback-rfrlcameracallback) or by calling [setOriginal(bitmap)](#setoriginalbitmap-bitmap).

Recognition should be performed before creative generation.

### startCreativeGeneration(resultCallback: [RfrlCreativeGenerationCallback](#RfrlCreativeGenerationCallback))

After setting original with [camera](#startcameraactivity-activity-resultcallback-rfrlcameracallback), this step will create the image or video with selected effect applied.

Arguments:

- resultCallback: [RfrlCreativeGenerationCallback](#RfrlCreativeGenerationCallback) — completion callback.

### startModeration(resultCallback: [RfrlModerationCallback](#RfrlModerationCallback))

Launches moderation of creative. It must be [generated](#startcreativegenerationresultcallback-rfrlcreativegenerationcallback) first.

### startManualModeration(file: File, resultCallback: [RfrlModerationCallback](#RfrlModerationCallback))

Starts moderation of arbitarty video file. Does not require creative to be generated first.

Arguments:

* file: File — video file, must be readable.
* resultCallback: [RfrlModerationCallback](#RfrlModerationCallback) — completion callback.

### startManualModeration(bitmap: Bitmap, resultCallback: [RfrlModerationCallback](#RfrlModerationCallback))

Starts moderation of arbitarty photo. Does not require creative to be generated first.

Arguments:

* bitmap: Bitmap — image bitmap.
* resultCallback: [RfrlModerationCallback](#RfrlModerationCallback) — completion callback.

## RfrlInitConfig

SDK configuration.

* context: Context — Android context.
* clientToken: String — SDK client token.

## RfrlInitCallback, RfrlInitResult, RfrlInitStatus

SDK initialization callback.

* onInitComplete(result: RfrlInitResult)

RfrlInitResult contains initialization status RfrlInitStatus:

* SUCCESS — initialization successfully completed.
* STILL_IN_PROGRESS — duplicated call before previous init request completed.
* ERROR_INVALID_TOKEN — invalid SDK client token.

## RfrlCameraCallback

Callback called after camera screen is closed.

- onCameraComplete(result: [RfrlCameraResult](#RfrlCameraResult))

## RfrlCameraResult

* ctx: [RfrlContext](#RfrlContext) — SDK context from which camera screen was started.
* canceled: Boolean — true, if the user nor took photo, nor selected image from the device Gallery.
* fromCamera: Boolean — true, if the user took photo with camera; false — if the user selected photo from the device Gallery.
* frontCamera: Boolean — true, if the user took photo by front camera.

## RfrlRecognitionCallback

- onRecognitionComplete(result: [RfrlRecognitionResult](#RfrlRecognitionResult))

## RfrlRecognitionResult

* status: [RfrlRecognitionStatus](#RfrlRecognitionStatus) — recognition completion status.
* reasons: Map<[RfrlRecognitionReason](#RfrlRecognitionReason), Boolean> — list of triggered recognition decline reasons. Reasons map to “false” values.

## RfrlRecognitionStatus

* PENDING — recognition is still in progress (inner SDK status, not exposed).
* APPROVED — no triggered recognition decline reasons found.
* DECLINED — there are triggered recognition decline reasons. In [RfrlRecognitionResult](#RfrlRecognitionResult), reasons field will contain list of "[reason](#RfrlRecognitionReason)" - "decline reason" pairs.
* FAILED — inner SDK error (network error, out of memory, etc.).
* BAD_INPUT — original bitmap [was not set](#setoriginalbitmap-bitmap).

## RfrlRecognitionReason

Recognition service decline reason:

* EXPLICIT
* WEAPON
* NUDE
* AGE_PREDICT
* CELEBRITY
* PIC_OR_MEME
* BEAUTY
* SCREEN_OR_PAPER
* SMOKE

## RfrlCreativeGenerationCallback

Callback called when creative generation is complete.

* onGenerationComplete(result: [RfrlGenerationResult](#RfrlGenerationResult))

## RfrlGenerationResult

* status: [RfrlGenerationStatus](#RfrlGenerationStatus) — creative generation completion status.
* isVideo: Boolean — true for video creatives.
* image: Bitmap? — output bitmap creative.
* videoFile: File? — output video creative file.

## RfrlGenerationStatus

* PENDING — generation is still in progress (inner SDK status, not exposed).
* COMPLETED — creative generation is successfully completed. Output will be placed in [RfrlGenerationResult](#RfrlGenerationResult) in image or videoFile fields, depending on creative type (image or video).
* FAILED — generation failed (network error, out of memory, server error, etc.).
* BAD_INPUT — invalid input ([camera](#startcameraactivity-activity-resultcallback-rfrlcameracallback) must be called first).

## RfrlModerationCallback

Callback called on completion of creative or arbitrary photo/video moderation.

* onModerationComplete(result: [RfrlModerationResult](#RfrlModerationResult))

## RfrlModerationResult

* status: [RfrlModerationStatus](#RfrlModerationStatus) — moderation completion status.
* reason: [RfrlModerationReason](#RfrlModerationReason)? — moderation decline reason for status == DECLINED.

## RfrlModerationStatus

* PENDING — moderation is still in progress (inner SDK status, not exposed).
* APPROVED — moderation is successfully completed.
* DECLINED — creative or photo/video file did not pass moderation with [RfrlModerationResult](#RfrlModerationResult) decline reason.
* EXPIRED — moderation failed due to timeout.

## RfrlModerationReason

* LOW_QUALITY
* BAD_COMPOSITION
* AGE_PREDICT
* FORBIDDEN_STUFF
* CELEBRITY
* OTHER_BRANDS
* OTHER
