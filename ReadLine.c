/* Alec Snyder 
 * C/Java JNI for gnu readline
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <readline/readline.h> 
#include <readline/history.h>
#include <jni.h>
#include "JSH.h"

//char *glob=0;

JNIEXPORT jstring JNICALL Java_JSH_readLine(JNIEnv *env, jobject obj,jstring pr,jint single)
{
    jclass this=(*env)->GetObjectClass(env, obj);
    jfieldID fid=(*env)->GetFieldID(env, this, "pointer", "J");
    jlong lastPointer=(*env)->GetLongField(env, this, fid);
    if(single)
    {
        lastPointer=0;
    }
    char *glob=(char *)lastPointer;
    if(glob==NULL)
    {
        glob=(char *)malloc(sizeof(char *));
    }
    else
    {
        free(glob);
    }
    const char *prompt=(*env)->GetStringUTFChars(env, pr, 0);
    char *line=readline(prompt);
    (*env)->ReleaseStringUTFChars(env, pr, prompt);
    glob=line;
    add_history(line);
    (*env)->SetLongField(env, this, fid, (jlong)glob);
    return (*env)->NewStringUTF(env, line);
}
