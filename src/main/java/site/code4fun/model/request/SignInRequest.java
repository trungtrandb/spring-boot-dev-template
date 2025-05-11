package site.code4fun.model.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
	private String provider;
	private String userName;
	private String password;
	private String idToken;
	private String code;
	private boolean rememberMe;
	private String redirectUrl;
}
