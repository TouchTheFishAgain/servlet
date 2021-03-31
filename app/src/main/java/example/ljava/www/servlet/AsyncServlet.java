package example.ljava.www.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncServlet extends HttpServlet {

    private Map<String, AsyncContext> rMap = new HashMap<>();

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String id = req.getParameter("id");

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");

        if (id == null) {
            resp.getWriter().println("id 不能为空");
            return;
        }

        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(90000);
        final Logger log = AsyncServlet.log;
        asyncContext.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                log.info("onComplete {}", id);
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                log.info("onTimeout {}", id);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                log.info("onError {}", id);
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                log.info("onStartAsync {}", id);
            }
        });

        synchronized (this.rMap) {
            AsyncContext prev = this.rMap.put(id, asyncContext);
            if (prev != null) {
                prev.complete();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String id = req.getParameter("id");
        final String msg = req.getParameter("msg");

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");
        if (id == null) {
            resp.getWriter().println("id 不能为空");
            return;
        }

        AsyncContext asyncContext = null;

        synchronized (this.rMap) {
            asyncContext = this.rMap.get(id);
            if (asyncContext != null)
                this.rMap.remove(id);
        }

        if (asyncContext == null) {
            resp.getWriter().println("id 表示的请求不存在");
            return;
        }

        try {
            asyncContext.getResponse().getWriter().println(String.format("id %s, 消息 %s", id, msg));
            asyncContext.complete();
            resp.getWriter().println("消息已送达");
        } catch (Exception e) {
            log.error("", e);
            resp.getWriter().println("发生异常 " + e.getMessage());
        }
    }
}