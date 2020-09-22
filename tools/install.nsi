; Used to generate Windows installers via NullSoft Install System (NSIS).

; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "EduForce Dashboard"
!define PRODUCT_VERSION "1.1.2"
!define PRODUCT_PUBLISHER "BioForce Analytics, LLC"
!define PRODUCT_WEB_SITE "http://bioforceanalytics.com/"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\dashboard.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "..\src\main\resources\com\bioforceanalytics\dashboard\images\bfa.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "..\LICENSE.txt"
; Components page
!insertmacro MUI_PAGE_COMPONENTS
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\dashboard.exe"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "..\target\${PRODUCT_NAME} ${PRODUCT_VERSION} Installer.exe"
InstallDir "$PROGRAMFILES\EduForce Dashboard"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

Section "EduForce Dashboard" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "/oname=dashboard.exe" "..\target\dashboard-${PRODUCT_VERSION}.exe"
  CreateDirectory "$SMPROGRAMS\EduForce Dashboard"
  CreateShortCut "$SMPROGRAMS\EduForce Dashboard\EduForce Dashboard.lnk" "$INSTDIR\dashboard.exe"
  CreateShortCut "$DESKTOP\EduForce Dashboard.lnk" "$INSTDIR\dashboard.exe"
SectionEnd

Section "Java Runtime Environment" SEC02
  SetOutPath "$INSTDIR\jre"
  SetOverwrite ifnewer
  File /r "$%JAVA_HOME%\jre\"
SectionEnd

Section "FFmpeg" SEC03
  SetOutPath "$INSTDIR\ffmpeg"
  SetOverwrite ifnewer
  File /r "..\ffmpeg\win64"
SectionEnd

;===================
; NOT FOR PRODUCTION
;===================
; Section "Debug Version" SEC04
;   SetOutPath "$INSTDIR"
;   SetOverwrite ifnewer
;   File "/oname=debug.exe" "..\target\dashboard-${PRODUCT_VERSION}-debug.exe"
; SectionEnd

Section -AdditionalIcons
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateShortCut "$SMPROGRAMS\EduForce Dashboard\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\EduForce Dashboard\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\dashboard.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\dashboard.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

; Section descriptions
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC01} "Includes both Education and Advanced Mode, as well as the SINC Technology Graph and Data Analysis Graph."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC02} "Bundled version of JRE 8 to ensure compatibility with all systems."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC03} "Bundled version of FFmpeg. REQUIRED FOR SINC TECHNOLOGY."

  ;===================
  ; NOT FOR PRODUCTION
  ;===================
  ;!insertmacro MUI_DESCRIPTION_TEXT ${SEC04} "[NOT FOR PRODUCTION] A version of the Dashboard with console-logging output displayed."

!insertmacro MUI_FUNCTION_DESCRIPTION_END


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\dashboard.exe"
  Delete "$INSTDIR\debug.exe"
  
  RMDir /r "$INSTDIR\logs"
  RMDir /r "$INSTDIR\jre"
  RMDir /r "$INSTDIR\ffmpeg"

  Delete "$SMPROGRAMS\EduForce Dashboard\Uninstall.lnk"
  Delete "$SMPROGRAMS\EduForce Dashboard\Website.lnk"
  Delete "$DESKTOP\EduForce Dashboard.lnk"
  Delete "$SMPROGRAMS\EduForce Dashboard\EduForce Dashboard.lnk"

  RMDir "$SMPROGRAMS\EduForce Dashboard"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  SetAutoClose true
SectionEnd