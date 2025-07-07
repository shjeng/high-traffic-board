package kuke.board.common.snowflake;

import java.util.random.RandomGenerator;

public class Snowflake {

	// 비트 수 지정. 총합 = 1 + 41 + 10 + 12 = 64비트
	// 비트 구성 상수 정의
	private static final int UNUSED_BITS = 1;        // 항상 0으로 고정 (부호 비트)
	private static final int EPOCH_BITS = 41;        // timestamp에 사용할 비트 수
	private static final int NODE_ID_BITS = 10;      // 노드 ID에 사용할 비트 수
	private static final int SEQUENCE_BITS = 12;     // 같은 밀리초 내의 시퀀스 번호에 사용할 비트 수



	// 최대값 계산 (2^비트 수 - 1)
	//시퀀스는 0~4095까지 가능
	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1; // 최대 노드 ID: 1023
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1; // 최대 시퀀스: 4095

	// 랜덤 노드 ID (실제 운영에서는 노드별로 고정값을 설정하는 게 안전함)
	private final long nodeId = RandomGenerator.getDefault().nextLong(maxNodeId + 1);

	// UTC = 2024-01-01T00:00:00Z
	// 기준 시간 (epoch): 2024-01-01T00:00:00Z의 UTC 시간 (밀리초 단위)
	private final long startTimeMillis = 1704067200000L;

	// 마지막으로 ID를 생성한 시간 (ms 단위)
	private long lastTimeMillis = startTimeMillis;

	// 같은 밀리초 내에 생성된 ID의 시퀀스 번호
	private long sequence = 0L;

	/**
	 * 고유 ID 생성 메서드 (synchronized: 스레드 안전)
	 */

	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis(); // 현재 시간(ms)

		// 시스템 시간이 과거로 돌아간 경우 예외 발생
		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		if (currentTimeMillis == lastTimeMillis) {
			// 같은 밀리초 내라면 시퀀스 증가
			sequence = (sequence + 1) & maxSequence;

			// 시퀀스 최대값(4095)을 초과한 경우 → 다음 밀리초까지 대기
			if (sequence == 0) {
				currentTimeMillis = waitNextMillis(currentTimeMillis);
			}
		} else {
			// 새로운 밀리초로 진입하면 시퀀스를 초기화
			sequence = 0;
		}

		// 마지막 생성 시간 업데이트
		lastTimeMillis = currentTimeMillis;

		// 비트 시프트 연산을 통해 최종 ID 구성
		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS))  // 41비트 timestamp
			| (nodeId << SEQUENCE_BITS) // 10비트 nodeId
			| sequence;  // 12비트 sequence
	}

	/**
	 * 시퀀스가 초과되었을 경우, 다음 밀리초가 올 때까지 대기
	 */
	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis(); // 바뀔 때까지 반복
		}
		return currentTimestamp;
	}
}
