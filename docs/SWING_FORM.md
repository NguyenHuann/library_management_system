# Chỉnh giao diện bằng NetBeans Swing Form

Mỗi màn hình có một file `.java` chứa logic và một file `.form` chứa cây thành phần, thuộc tính, layout và ánh xạ sự kiện. NetBeans dùng `.form` để mở tab **Design** và sinh `initComponents()`.

## Mở và chỉnh sửa

1. Mở dự án Maven trong NetBeans bằng **File → Open Project**.
2. Vào **Source Packages → com.mycompany.library_management_system.ui**.
3. Nhấn đúp `LauncherFrame.java`, `ServerFrame.java` hoặc `ClientFrame.java` rồi chọn **Design** cạnh tab **Source**.
4. Chọn widget trong **Navigator** hoặc trên vùng thiết kế. Chỉnh `text`, `font`, `foreground`, `background`, `preferredSize`, `border` trong **Properties**.
5. Thêm JButton/JLabel/JTextField từ **Palette**; dùng layout/constraints để đặt vị trí. Các Form dùng layout chuẩn Swing, không cần thư viện AbsoluteLayout.
6. Lưu file. **Run Project (F6)** mở màn hình chọn chế độ; **Run File (Shift+F6)** chạy Form đang mở.

## Hai trang của ClientFrame

`root` dùng `CardLayout`, có hai trang:

- `connectionPage`, tên card `connect`: IP, cổng, nút kết nối và thông báo lỗi.
- `managementPage`, tên card `manage`: thống kê, tìm kiếm, JTable, nhập sách và CRUD.

Trong Navigator, mở `[JFrame] → root` và chọn trang muốn chỉnh. Có thể dùng menu chuột phải **Design This Container** để tập trung thiết kế trang đó, sau đó **Design Parent** để trở về. Khi chạy, chương trình tự chuyển trang sau khi kết nối hoặc ngắt kết nối.

## Sự kiện và logic

Các nút có sự kiện `actionPerformed` được khai báo trong Form. Chọn nút, vào **Properties → Events → actionPerformed**, hoặc nhấn đúp nút để mở hàm xử lý.

- Client: kết nối/ngắt kết nối, tìm, làm mới, sách mới, thêm, lưu và xóa.
- Server: chọn đường dẫn database, khởi động và dừng.
- Launcher: mở cửa sổ Server hoặc Client.

Các hàm này gọi phần xử lý có sẵn ở ngoài vùng sinh tự động. Tác vụ mạng/database chạy trong SwingWorker để giữ giao diện phản hồi. Bộ định thời làm mới danh mục mỗi 8 giây được tạo sau `initComponents()`; mẫu bảng và renderer được cấu hình lúc chạy.

## Quy tắc khi sửa

- Không xóa/đổi tên `.form` tách rời `.java`. Dùng **Refactor → Rename** trong NetBeans nếu cần đổi tên lớp.
- Không chỉnh tay vùng `initComponents()` và khai báo biến có dấu `GEN-BEGIN` / `GEN-END`; NetBeans có thể sinh lại toàn bộ vùng này.
- Các biến như `host`, `port`, `table`, `code`, `title`, `author`, `year`, `quantity` đang được logic sử dụng. Khi đổi tên bằng tab Code/Properties, dùng công cụ refactor và kiểm tra các tham chiếu.
- Dữ liệu mẫu của bảng trong Design chỉ giúp thiết kế; khi chạy, `BookTableModel` thay bằng dữ liệu nhận từ Server.
- Dùng `mvn verify` / `build.cmd` để kiểm tra lại sau khi thay đổi. Đóng cửa sổ chạy từ file JAR trước khi đóng gói lại trên Windows.

SQL, giao thức TCP và các lớp Server/Client độc lập với phần bố cục Form, nên chỉnh vị trí, màu, font hoặc nhãn không cần thay đổi database.
