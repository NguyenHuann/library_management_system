# BÁO CÁO THỰC HÀNH: HỆ THỐNG QUẢN LÝ THƯ VIỆN QUA MẠNG

**Công nghệ:** Java 17 trở lên, Java Swing, TCP Socket, SQLite, JDBC, Gson, Maven.  
**Mô hình:** Một server quản lý dữ liệu tập trung, nhiều client cùng kết nối qua mạng LAN.  
**Cổng mặc định:** TCP `9999`.

## 1. Mục tiêu và chức năng đã thực hiện

Ứng dụng cho phép nhân viên thư viện thêm sách, xem danh sách, tìm kiếm theo mã hoặc tên, cập nhật và xóa sách. Màn hình kết nối cho phép nhập địa chỉ IP và cổng của server. Giao diện quản lý hiển thị danh sách, thông tin cuốn sách được chọn và các thống kê số đầu sách, tổng số bản, số đầu sách có ít bản. Mọi thao tác mạng chạy ngoài luồng giao diện để cửa sổ vẫn phản hồi khi chờ server.

| Yêu cầu của đề bài | Cách thực hiện |
| --- | --- |
| Server đa luồng, TCP 9999 | `ServerSocket` chấp nhận kết nối; `ExecutorService` xử lý từng client độc lập. |
| Client kết nối và hiển thị phản hồi | `Socket`, màn hình nhập IP/cổng, trạng thái kết nối và thông báo lỗi. |
| Năm chức năng quản lý sách | Thêm, liệt kê, tìm kiếm, cập nhật và xóa thông qua các lệnh gửi server. |
| Xác thực và phân quyền | Server gán ADMIN cho thủ thư và READER cho bạn đọc; lệnh thêm/sửa/xóa được kiểm tra phía Server. |
| Mượn/trả | Phiếu mượn theo tài khoản, lịch sử riêng và cập nhật tồn kho có transaction. |
| UDP và nhật ký | Thông báo danh mục tới Client đã đăng nhập; nhật ký thao tác có tên tài khoản. |
| Giao thức, kiểm tra dữ liệu và lỗi | JSON UTF-8 có tiền tố độ dài; phản hồi thành công/thất bại và mã lỗi. |
| Giao diện, ít nhất hai client | Java Swing cho cả server và client; kiểm thử tích hợp bằng hai socket đồng thời. |

Đã thực hiện cả bốn phần nâng cao trong đề, cùng phát hiện xung đột khi hai Client sửa cùng một sách.

## 2. Kiến trúc và luồng xử lý

```text
Client Swing A ── TCP Socket ──┐
                              ├── ServerSocket + các worker ── JDBC ── SQLite
Client Swing B ── TCP Socket ──┘
```

Client gửi thông tin đăng nhập qua kết nối TCP. Server xác minh tài khoản bằng PBKDF2-HMAC-SHA256 với salt riêng; chỉ sau xác thực Server mới nhận lệnh sách hoặc phiếu mượn từ phiên đó. Server đọc trọn từng thông điệp, kiểm tra lệnh và dữ liệu, gọi repository SQLite rồi trả kết quả. Client không mở JDBC và không đọc trực tiếp file database.

Server ghi kết nối, ngắt kết nối và lệnh TCP vào `logs/server.log`; ghi tài khoản, hành động, mã sách/phiếu và kết quả vào `logs/client-activity.log`. Hai file UTF-8 không ghi mật khẩu hoặc UDP token. Giao diện Server hiển thị nhật ký chính và số client hiện tại. Khi Server dừng, các socket được đóng; Client báo mất kết nối.

Tài khoản demo là `admin` / `admin123` (ADMIN) và `bandoc` / `bandoc123` (READER), chỉ tự tạo khi ứng dụng tạo bảng `TaiKhoan`. Database cũ chỉ chứa bảng sách được tự bổ sung schema và tài khoản mẫu, đồng thời giữ nguyên sách. Khi bổ sung cột Role trên database cũ, tài khoản `admin` hiện có nhận vai trò ADMIN; tài khoản khác nhận READER. Hash mật khẩu hiện có được giữ nguyên.

## 3. Cơ sở dữ liệu và tính nhất quán

`Sach` giữ năm trường theo đề và cột `Version` để kiểm tra cập nhật đồng thời. `TaiKhoan` lưu username, salt PBKDF2-HMAC-SHA256 (600.000 vòng) và password hash. `PhieuMuon` lưu tài khoản, mã/tên sách chụp tại thời điểm mượn và thời gian mượn/trả. Lịch sử mỗi tài khoản được lọc ở phía Server; phiếu vẫn tồn tại nếu sách đã xóa sau khi trả. Schema mở rộng ở `src/main/resources/advanced.sql`. Mật khẩu demo khi tạo mới CSDL: `admin` / `admin123`, `bandoc` / `bandoc123`. Database đã có bảng `TaiKhoan` được giữ nguyên tài khoản hiện có; không tự thêm user demo nếu bảng đó tồn tại trước khi cập nhật ứng dụng.

Server kiểm tra mã sách dài 1–10 ký tự, tên và tác giả dài 1–100 ký tự, năm xuất bản từ 1 đến năm hiện tại cộng một, số lượng không âm. Trường rỗng, ký tự điều khiển và số không đúng kiểu bị từ chối. `PreparedStatement` tách dữ liệu người dùng khỏi câu lệnh SQL. Tìm kiếm chuẩn hóa chữ hoa/thường và dấu tiếng Việt để hỗ trợ từ khóa như `lap trinh`.

Các thao tác cập nhật/xóa sử dụng điều kiện `WHERE MaSach = ? AND Version = ?`; xóa bị từ chối khi còn phiếu mượn chưa trả. Mượn sách giảm tồn kho, tăng version và lập phiếu trong một transaction; trả sách chốt phiếu thuộc chính tài khoản đăng nhập, tăng tồn kho và version trong cùng transaction. SQLite WAL, busy timeout, câu lệnh tham số và khóa ghi đảm bảo không mượn âm kho khi các Client tranh chấp bản cuối. Hai Client sửa một version sách chỉ có một thao tác thành công.

## 4. Giao thức trao đổi

Mỗi thông điệp gồm **4 byte số nguyên big-endian biểu thị số byte JSON**, tiếp theo là JSON mã hóa UTF-8. Giới hạn một thông điệp là 1 MiB. Cơ chế độ dài cho phép nhận đúng dữ liệu dù TCP chia một yêu cầu thành nhiều lần đọc hoặc gộp nhiều yêu cầu liên tiếp. Sau lệnh `PING` bắt đầu phiên, Client gửi `LOGIN`; các lệnh đọc/ghi cần đăng nhập cùng TCP connection.

Yêu cầu có các trường `command`, `book`, `query`, `code`, `version`; chỉ các trường phù hợp với từng lệnh cần có dữ liệu. Đối tượng `book` chứa `code`, `title`, `author`, `year`, `quantity`, `version`.

| Lệnh | Dữ liệu gửi | Kết quả |
| --- | --- | --- |
| `LIST` | Không cần tham số | Toàn bộ danh sách sách. |
| `SEARCH` | `query` | Sách có mã hoặc tên chứa từ khóa. |
| `ADD` | `book` | Sách vừa thêm hoặc lỗi trùng mã/dữ liệu. |
| `UPDATE` | `book`, gồm phiên bản đã tải | Sách sau cập nhật hoặc lỗi không tồn tại/xung đột. |
| `DELETE` | `code`, `version` | Xác nhận xóa hoặc lỗi. |
| `LOGIN` | `username`, `password`, `udpPort`, `udpToken` | Phiên đăng nhập; mật khẩu không ghi vào file log. |
| `BORROW` | `code` | Tạo phiếu, trừ một bản khả dụng. |
| `RETURN` | `loanId` | Trả một phiếu đang mở thuộc người dùng hiện tại. |
| `LOANS` | Không | Lịch sử mượn/trả riêng tài khoản. |
| `PING`, `QUIT` | Không cần tham số | Kiểm tra hoặc kết thúc phiên kết nối. |

Sau thay đổi sách hoặc mượn/trả, Server phát một JSON `LibraryNotice` bằng UDP tới Client đã đăng ký port trên cùng phiên TCP, xác thực datagram bằng token ngẫu nhiên của phiên. Loại sự kiện gồm `BOOK_ADDED`, `BOOK_UPDATED`, `BOOK_DELETED`, `BOOK_BORROWED`, `BOOK_RETURNED`. UDP có thể mất gói; danh sách/phiếu TCP vẫn là nguồn dữ liệu chuẩn.

Phản hồi thành công có dạng `{"success":true,"message":"…","books":[]}`; trường `errorCode` được bỏ qua khi không có lỗi. Khi thất bại, `success` bằng `false` và `errorCode` mô tả nhóm lỗi như `VALIDATION`, `DUPLICATE`, `NOT_FOUND`, `CONFLICT`, `UNKNOWN_COMMAND`, `PROTOCOL` hoặc `DATABASE`. JSON sai cú pháp, UTF-8 lỗi, số dạng chuỗi, số thập phân tại trường số nguyên và khung vượt kích thước bị từ chối. Lỗi giao thức chỉ ảnh hưởng kết nối gửi dữ liệu sai; các client còn lại tiếp tục hoạt động.

## 5. Chạy ứng dụng và kiểm thử

Biên dịch và chạy kiểm thử bằng `mvn clean verify` hoặc `build.cmd`. Mở `run-server.cmd`, chọn **Khởi động server**, sau đó mở `run-client.cmd` hai lần và đăng nhập vào `127.0.0.1:9999`. Trên máy khác trong LAN, thay `127.0.0.1` bằng IPv4 của máy Server và cho phép TCP 9999 qua tường lửa. Server dùng TCP cổng cấu hình mặc định 9999 và UDP trên cổng được cấp tự động để gửi thông báo. Có thể chạy Server không giao diện bằng `java -jar target/library-management.jar --server --headless`.

Kiểm thử tự động dùng Server TCP thực với cổng hệ điều hành cấp và SQLite tạm riêng cho từng ca. Nội dung bao gồm CRUD, tài khoản sai/đúng, lệnh chưa xác thực, không ghi mật khẩu, mượn/trả, kho cạn, mượn bản cuối cạnh tranh, lịch sử tách theo tài khoản, xóa khi còn phiếu đang mượn, UDP, nhật ký, lưu dữ liệu và khung TCP/JSON lỗi.

**Kết quả đã xác minh:** `LibraryServerIntegrationTest` chạy **50 ca, 0 thất bại, 0 lỗi, 0 bỏ qua** bằng `mvn verify`; kết quả chi tiết nằm trong `target/surefire-reports/` và bản sao tại [KET_QUA_KIEM_THU.txt](KET_QUA_KIEM_THU.txt). Ca mới kiểm tra bạn đọc không thể thực hiện CRUD qua các TCP command trực tiếp.

Kiểm tra trực quan đã mở hai ứng dụng Client Swing kết nối đồng thời đến Server tại `127.0.0.1:9999`. Server hiển thị **2** phiên, nhật ký nhận lệnh từ hai cổng nguồn khác nhau; Client tải được tám đầu sách mẫu và chọn dòng để chỉnh sửa. Thử nghiệm này dùng TCP loopback trên cùng máy; kết nối giữa hai máy vật lý cần xác minh thêm trên LAN thực tế.

Ảnh minh chứng đi kèm:

- [Màn hình kết nối](images/01-ket-noi.png).
- [Giao diện quản lý sách](images/02-quan-ly-sach.png).
- [Server ghi nhận hai client đồng thời](images/03-server-hai-client.png).

Bản ghi kết quả kiểm thử tự động được lưu kèm tại [KET_QUA_KIEM_THU.txt](KET_QUA_KIEM_THU.txt).

