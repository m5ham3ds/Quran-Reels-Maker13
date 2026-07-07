import re
with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "r") as f:
    content = f.read()

target = """            } catch (e: kotlinx.coroutines.CancellationException) {
                SystemDiagnosticTracker.addLog("PROCESS_CANCEL", "تم إلغاء عملية المونتاج من قبل المستخدم")
                _serviceState.value = ReelState.Idle
                stopForeground(true)
                stopSelf()
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: "Unknown error occurred"
                _serviceState.value = ReelState.Error(errMsg)
                showErrorNotification(errMsg, isArabic)
                stopForeground(true)
                stopSelf()
            }"""

replacement = """            } catch (e: kotlinx.coroutines.CancellationException) {
                SystemDiagnosticTracker.addLog("PROCESS_CANCEL", "تم إلغاء عملية المونتاج من قبل المستخدم")
                if (activeJob == kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]) {
                    _serviceState.value = ReelState.Idle
                    stopForeground(true)
                    stopSelf()
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: "Unknown error occurred"
                if (activeJob == kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]) {
                    _serviceState.value = ReelState.Error(errMsg)
                    showErrorNotification(errMsg, isArabic)
                    stopForeground(true)
                    stopSelf()
                }
            }"""

content = content.replace(target, replacement)

target2 = """                    onComplete = { uri ->
                        _serviceState.value = ReelState.Success(uri)
                        showCompleteNotification(uri, isArabic)
                        stopForeground(true)
                        stopSelf()
                    },
                    onError = { err ->
                        _serviceState.value = ReelState.Error(err)
                        showErrorNotification(err, isArabic)
                        stopForeground(true)
                        stopSelf()
                    }"""

replacement2 = """                    onComplete = { uri ->
                        if (activeJob == kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]) {
                            _serviceState.value = ReelState.Success(uri)
                            showCompleteNotification(uri, isArabic)
                            stopForeground(true)
                            stopSelf()
                        }
                    },
                    onError = { err ->
                        if (activeJob == kotlinx.coroutines.currentCoroutineContext()[kotlinx.coroutines.Job]) {
                            _serviceState.value = ReelState.Error(err)
                            showErrorNotification(err, isArabic)
                            stopForeground(true)
                            stopSelf()
                        }
                    }"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/service/VideoGenerationService.kt", "w") as f:
    f.write(content)
