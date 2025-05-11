package site.code4fun.service.ai;

public interface ModelName {
    String GEMINI_2_FLASH = "gemini-2.0-flash";
    String GEMINI_2_5_PRO_EXP = "gemini-2.5-pro-exp-03-25";
    String GEMINI_2_FLASH_STABLE = "gemini-2.0-flash-001";
    String GEMINI_1_5_FLASH = "gemini-1.5-flash";
    String GEMINI_1_5_PRO = "gemini-1.5-pro";
    String GEMINI_1_PRO = "gemini-1.0-pro";


    String GPT_4 = "gpt-4o"; //10,000 TPM. 3 RPM 200 RPD. 90,000 TPD
    String GPT_4_MINI = "gpt-4o-mini"; // 60,000 TPM. 3 RPM 200 RPD. 200,000 TPD
    String GPT_3_5_TURBO = "gpt-3.5-turbo";
    String GPT_EMBEDDING_3_SMALL = "text-embedding-3-small";

    String DEEPSEEK_CHAT = "deepseek-chat";
    String DEEPSEEK_REASONER = "deepseek-reasoner";
}

