/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.simp.stomp;

import java.nio.ByteBuffer;

import reactor.fn.Consumer;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;

import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * A Reactor TCP {@link Codec} for sending and receiving STOMP messages.
 *
 * @author Andy Wilkinson
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class Reactor2StompCodec extends Codec<Buffer, Message<byte[]>, Message<byte[]>> {

	private final Function<Message<byte[]>, Buffer> encodingFunction;

	private final StompDecoder stompDecoder;


	public Reactor2StompCodec() {
		this(new StompEncoder(), new StompDecoder());
	}

	public Reactor2StompCodec(StompEncoder encoder, StompDecoder decoder) {
		Assert.notNull(encoder, "StompEncoder is required");
		Assert.notNull(decoder, "StompDecoder is required");
		this.encodingFunction = new EncodingFunction(encoder);
		this.stompDecoder = decoder;
	}


	@Override
	public Function<Buffer, Message<byte[]>> decoder(final Consumer<Message<byte[]>> messageConsumer) {
		return new DecodingFunction(this.stompDecoder, messageConsumer);
	}

	@Override
	public Function<Message<byte[]>, Buffer> encoder() {
		return this.encodingFunction;
	}

	@Override
	public Buffer apply(Message<byte[]> message) {
		return this.encodingFunction.apply(message);
	}


	private static class EncodingFunction implements Function<Message<byte[]>, Buffer> {

		private final StompEncoder encoder;

		public EncodingFunction(StompEncoder encoder) {
			this.encoder = encoder;
		}

		@Override
		public Buffer apply(Message<byte[]> message) {
			byte[] bytes = this.encoder.encode(message);
			return new Buffer(ByteBuffer.wrap(bytes));
		}
	}


	private static class DecodingFunction implements Function<Buffer, Message<byte[]>> {

		private final StompDecoder decoder;

		private final Consumer<Message<byte[]>> messageConsumer;

		public DecodingFunction(StompDecoder decoder, Consumer<Message<byte[]>> next) {
			this.decoder = decoder;
			this.messageConsumer = next;
		}

		@Override
		public Message<byte[]> apply(Buffer buffer) {
			for (Message<byte[]> message : this.decoder.decode(buffer.byteBuffer())) {
				this.messageConsumer.accept(message);
			}
			return null;
		}
	}

}
