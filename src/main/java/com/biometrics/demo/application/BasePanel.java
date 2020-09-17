package com.biometrics.demo.application;

import com.neurotec.util.concurrent.AggregateExecutionException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BasePanel extends JPanel {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	// ===========================================================
	// Protected fields
	// ===========================================================

	protected LicensingPanel licensing;
	protected final List<String> requiredLicenses = new ArrayList<String>();
	protected final List<String> optionalLicenses = new ArrayList<String>();
	protected boolean obtained;

	// ===========================================================
	// Public methods
	// ===========================================================

	public void init() {
		initGUI();
		setDefaultValues();
		updateControls();
	}

	public final List<String> getRequiredLicenses() {
		return requiredLicenses;
	}

	public final List<String> getOptionalLicenses() {
		return optionalLicenses;
	}

	public final LicensingPanel getLicensing() {
		return licensing;
	}

	public final void updateLicensing(boolean status) {
		licensing.setComponentObtainingStatus(status);
		obtained = status;
	}

	public boolean isObtained() {
		return obtained;
	}

	public void showError(Throwable e) {
		e.printStackTrace();
		if (e instanceof AggregateExecutionException) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("Execution resulted in one or more errors:\n");
			for (Throwable cause : ((AggregateExecutionException) e).getCauses()) {
				sb.append(cause.toString()).append('\n');
			}
			JOptionPane.showMessageDialog(this, sb.toString(), "Execution failed", JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showError(String message) {
		if (message == null) throw new NullPointerException("message");
		JOptionPane.showMessageDialog(this, message);
	}

	// ===========================================================
	// Abstract methods
	// ===========================================================

	protected abstract void initGUI();
	protected abstract void setDefaultValues();
	protected abstract void updateControls();
	protected abstract void updateFacesTools();

	public abstract void onDestroy();
	public abstract void onClose();
}
