package kr.rea.keep;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
@SpringBootApplication
public class KeepApplication {
 public static void main(String[] args) { SpringApplication.run(KeepApplication.class, args); }
 @Bean Clock clock() { return Clock.systemUTC(); }
}
