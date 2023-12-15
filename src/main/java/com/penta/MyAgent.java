package com.penta;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class MyAgent {
    // JavaAgent를 로딩할 때 필요한 메서드, JavaAgent는 ClassLoad 시점에 실행
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()              // 클래스의 변형을 위한 빌더 클래스
                .type(ElementMatchers.any())    // 모든 타입에 대한 변형 지정
                // builder: 변형될 클래스의 빌더. 이를 통해 클래스의 메서드, 필드 등을 조작할 수 있음.
                // typeDescription: 변형될 클래스의 설명을 나타내는 객체
                // classLoader: 클래스가 로드된 클래스 로더
                // javaModule: 클래스가 속한 Java 모듈
                .transform((builder, typeDescription, classLoader, javaModule) -> builder.method(named("render"))   // render() 메서드에 대해
                        .intercept(MethodDelegation.to(MyInterceptor.class)))       // render라는 이름의 메서드를 가진 모든 메서드에 대해 MyInterceptor 클래스로의 메서드 위임을 추가하는 변형 작업을 수행
                .installOn(inst);   // 정의한 변형 작업을 실제로 적용해 클래스 변형을 적용. inst는 클래스 로딩 시 바이트 코드를 변형할 수 있도록 하는데 사용
    }

    // 함수 호출 확인
    public static class MyInterceptor {
        static int callCount;

        public static String intercept(@SuperCall Callable<String> zuper) throws Exception {
            zuper.call();
            callCount++;
            return callCount + " 번 호출하셨습니다.";
        }
    }
}