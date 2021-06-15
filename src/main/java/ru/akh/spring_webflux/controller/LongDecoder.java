package ru.akh.spring_webflux.controller;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class LongDecoder extends AbstractDataBufferDecoder<Long> {

    public LongDecoder() {
        super(MimeTypeUtils.TEXT_PLAIN);
    }

    @Override
    public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
        return (elementType.resolve() == Long.class && super.canDecode(elementType, mimeType));
    }

    @Override
    public Long decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints)
            throws DecodingException {
        Charset charset = getCharset(mimeType);
        CharBuffer charBuffer = charset.decode(buffer.asByteBuffer());
        DataBufferUtils.release(buffer);
        String value = charBuffer.toString();
        return Long.valueOf(value);
    }

    private Charset getCharset(@Nullable MimeType mimeType) {
        if (mimeType != null && mimeType.getCharset() != null) {
            return mimeType.getCharset();
        }
        return StandardCharsets.UTF_8;
    }

}
