package foghandoff.fog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import static foghandoff.fog.FogMessages.Location;

@Component
@Getter
@Setter
@AllArgsConstructor
public class Member implements Serializable {
   private String id;
   private Location longitude;
   // Needed for Spring to not complain.
   public Member() {}
   public String toString() {
   		return id;
   }
}