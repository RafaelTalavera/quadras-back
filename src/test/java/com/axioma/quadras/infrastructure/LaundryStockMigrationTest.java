package com.axioma.quadras.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class LaundryStockMigrationTest {

	private static final List<String> LEDGER_TABLES = List.of(
			"laundry_stock_balances",
			"laundry_stock_lots",
			"laundry_stock_movements",
			"laundry_obligations",
			"laundry_opening_stock",
			"laundry_opening_stock_lines",
			"laundry_stock_adjustments",
			"laundry_idempotency_keys",
			"laundry_documents",
			"laundry_document_lines",
			"laundry_rewash_dispatch_allocations",
			"laundry_return_manifests",
			"laundry_return_manifest_lines",
			"laundry_return_allocations",
			"laundry_billable_events"
	);

	@Test
	void shouldCreateLedgerTablesOnEmptySchema() throws Exception {
		final String url = databaseUrl("empty");
		migrate(url, "45");

		assertTables(url);
		assertVersion(url, "42");
		assertVersion(url, "43");
		assertVersion(url, "44");
		assertVersion(url, "45");
	}

	@Test
	void shouldUpgradeV42SchemaWithoutDroppingExistingBusinessTables() throws Exception {
		final String url = databaseUrl("upgrade");
		migrate(url, "42");

		assertVersion(url, "42");
		assertThat(tableCount(url, "laundry_shipments")).isEqualTo(1);
		assertThat(tableCount(url, "laundry_stock_balances")).isZero();

		migrate(url, "45");

		assertVersion(url, "43");
		assertVersion(url, "44");
		assertVersion(url, "45");
		assertThat(tableCount(url, "laundry_shipments")).isEqualTo(1);
		assertTables(url);
	}

	private String databaseUrl(String suffix) {
		return "jdbc:h2:mem:laundry_stock_" + suffix + "_" + UUID.randomUUID()
				+ ";MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
	}

	private void migrate(String url, String target) {
		Flyway.configure()
				.dataSource(url, "sa", "")
				.locations("classpath:db/migration")
				.target(target)
				.load()
				.migrate();
	}

	private void assertTables(String url) throws Exception {
		for (String table : LEDGER_TABLES) {
			assertThat(tableCount(url, table)).as("table " + table).isEqualTo(1);
		}
	}

	private int tableCount(String url, String table) throws Exception {
		try (Connection connection = DriverManager.getConnection(url, "sa", "");
			 PreparedStatement statement = connection.prepareStatement(
					 "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ?")) {
			statement.setString(1, table.toUpperCase());
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		}
	}

	private void assertVersion(String url, String version) throws Exception {
		try (Connection connection = DriverManager.getConnection(url, "sa", "");
			 PreparedStatement statement = connection.prepareStatement(
					 "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = ? AND \"success\" = TRUE")) {
			statement.setString(1, version);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				assertThat(resultSet.getInt(1)).as("flyway version " + version).isEqualTo(1);
			}
		}
	}
}
