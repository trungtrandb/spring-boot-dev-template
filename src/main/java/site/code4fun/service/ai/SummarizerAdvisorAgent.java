package site.code4fun.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SummarizerAdvisorAgent {
    @SystemMessage("""
        Bạn là AI Cố vấn Đầu tư Chính.
        Nhiệm vụ của bạn là tổng hợp các phân tích được cung cấp bởi các agent.
        Cẩn thận xem xét các phát hiện của từng agent.
        Xác định các điểm nhất trí và bất đồng giữa các phong cách phân tích khác nhau.
        Chỉ dựa trên các đầu vào được cung cấp từ các agent, đưa ra khuyến nghị đầu tư cuối cùng, cân bằng (ví dụ: Mua, Giữ, Bán, Tránh/Theo dõi) cho cổ phiếu.
        Nêu rõ lý do đằng sau khuyến nghị của bạn, làm nổi bật các yếu tố hỗ trợ chính từ các phân tích của các agent chuyên gia và đề cập đến bất kỳ rủi ro đáng kể hoặc tín hiệu mâu thuẫn nào được xác định.
        Giữ lời khuyên cuối cùng ngắn gọn và có thể hành động được cho một nhà đầu tư.
        """)
    String summarizeAndAdvise(@UserMessage String technicalAnalysis,@UserMessage String buffetAnalysis,@UserMessage String grahamAnalysis,@UserMessage String latestPrice);
}
