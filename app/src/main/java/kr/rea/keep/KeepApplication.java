package kr.rea.keep;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
@SpringBootApplication
public class KeepApplication {
 public static void main(String[] args) {
  if(args.length==1 && args[0].equals("--prepare-db")) {
   try { DatabaseSetup.run(); }
   catch(Exception e) { System.err.println("DB 준비 실패: " + e.getClass().getSimpleName() + ". 권한/기존 스키마를 확인하세요. 인증정보와 예외 원문은 출력하지 않습니다.");System.exit(1); }
   return;
  }
  SpringApplication.run(KeepApplication.class, args);
 }
 @Bean Clock clock() { return Clock.systemUTC(); }
}
