package com.tabnine;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.tabnine.general.DependencyContainer;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		DependencyContainer.instanceOfBinaryRequestFacade().shutdown();
	}

}
