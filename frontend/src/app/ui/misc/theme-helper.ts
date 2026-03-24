import { CleanupJsonString } from "@common/forms/validation/custom-validator";
import { argbFromHex, themeFromSourceColor, applyTheme } from "@material/material-color-utilities";

const materialPrefixRe = /(--md-sys-color|--mat-sys-color|mat-sys-color|md-sys-color|--mat-sys|--md-sys|md-sys|mat-sys)/g;
const FONT_VARS = [
    "--mat-sys-body-large-size",
    "--mat-sys-body-medium-size",
    "--mat-sys-body-small-size",
    "--mat-sys-display-large-size",
    "--mat-sys-display-medium-size",
    "--mat-sys-display-small-size",
    "--mat-sys-headline-large-size",
    "--mat-sys-headline-medium-size",
    "--mat-sys-headline-small-size",
    "--mat-sys-label-large-size",
    "--mat-sys-label-medium-size",
    "--mat-sys-label-small-size",
    "--mat-sys-title-large-size",
    "--mat-sys-title-medium-size",
    "--mat-sys-title-small-size",
    "--mat-standard-button-toggle-label-text-size"
]

export function generateDynamicTheme(primaryColor: string) {
    const fallbackPrimary = '#18488F';

    let argbPrimary;
    try {
        argbPrimary = argbFromHex(primaryColor);
    } catch (error) {
        // falling to default color if it's invalid color
        argbPrimary = argbFromHex(fallbackPrimary);
    }

    const targetElement = document.documentElement;

    // Get the theme from a hex color
    const theme = themeFromSourceColor(argbPrimary, [
        {
            name: "custom-theme",
            value: argbPrimary,
            blend: true,
        }
    ]);


    // Apply the theme to the body by updating custom properties for material tokens
    applyTheme(theme, {target: targetElement, dark: false})
    // // Get the theme from a hex color
    // const theme = themeFromSourceColor(argbPrimary);

    // // Apply theme to root element
    // applyTheme(theme, {
    //   target: targetElement,
    //   dark: false,
    //   brightnessSuffix: true,
    // });

    // const styles = targetElement.style;

    // values are set as --md-sys. Need to be replaced with mat-sys
    for (const key in targetElement.style) {
      if (Object.prototype.hasOwnProperty.call(targetElement.style, key)) {
        const propName = targetElement.style[key];
        if (materialPrefixRe.test(propName)) {
          const sysPropName = '--mat-sys' + propName.replace(materialPrefixRe, '');
          targetElement.style.setProperty(
            sysPropName,
            targetElement.style.getPropertyValue(propName)
          );
        }
      }
    }
}

export function overrideCss(input: string){
    let json;
    const targetElement = document.documentElement;
    try {
        const value = CleanupJsonString(input);
        json = JSON.parse(value);
    } catch(error) {
        return;
    }
    for (const [key, rgba] of Object.entries(json)) {
        if (materialPrefixRe.test(key)) {
          const sysPropName = '--mat-sys' + key.replace(materialPrefixRe, '');
          targetElement.style.setProperty(
            sysPropName,
            rgba.toString()
          );
        }
        targetElement.style.setProperty(
            key,
            rgba.toString()
          );
    }
}

export function toggleFontSize(large: boolean){
    if(large){
        FONT_VARS.forEach((key) => {
            const prevValue = getComputedStyle(document.body).getPropertyValue(key);
            const newValue = Number(prevValue.replace('rem', '')) * 1.085;
            document.documentElement.style.setProperty(
                key,
                `${newValue}rem`
            )
        })
    } else {
       FONT_VARS.forEach((key) => {
        const prevValue = getComputedStyle(document.body).getPropertyValue(key);
        const newValue = Number(prevValue.replace('rem', '')) / 1.085;
        document.documentElement.style.setProperty(
            key,
            `${newValue}rem`
        )
       })     
    }
}

interface ColorData {
    tone: number;
    hex: string;
}